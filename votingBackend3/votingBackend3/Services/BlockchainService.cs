using Microsoft.EntityFrameworkCore;
using System.Xml.Linq;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;


namespace votingBackend3.Services
{
    public class BlockchainService
    {
        private readonly BlocksDbContext _blocksDbContext;
        private readonly HttpClient _httpClient;

        public BlockchainService(BlocksDbContext blocksDbContext, HttpClient httpClient)
        {
            _blocksDbContext = blocksDbContext;
            _httpClient = httpClient;
        }

        public async Task AddgenesisBlock()
        {
            var block_transaction = await _blocksDbContext.Database.BeginTransactionAsync();
            try
            {
                //check if a genesis block exist
                var hasBlocks = await _blocksDbContext.blocks.AnyAsync();
                if (hasBlocks) return;

                DateTime today = new DateTime().ToUniversalTime();
               
                //create genesis block
                Block genesisBlock = new Block(
                        0, 
                        new DateTime(today.Year, today.Month, today.Day, today.Hour, 0, 0, DateTimeKind.Utc),
                        "genesis block", 
                        ""
                    );



                //add to database
                _blocksDbContext.blocks.Add(genesisBlock);

                //save changes
                await _blocksDbContext.SaveChangesAsync();

                //commit changes
                await block_transaction.CommitAsync();
            }
            catch (Exception ex)
            {
                await block_transaction.RollbackAsync();
                throw new Exception(ex.Message);
            }
        }

        public async Task<Block> GetLatestBlock()
        {
            try
            {
                return await _blocksDbContext.blocks.OrderByDescending(b => b.index).FirstAsync();
            }
            catch (Exception ex) {
                throw new Exception(ex.Message);
            }
        }

        public async Task addBlock(Block newBlock)
        {
            var block_transaction = await _blocksDbContext.Database.BeginTransactionAsync();
            try
            {
                newBlock.previousHash = (await GetLatestBlock()).hash;
                //add to database
                _blocksDbContext.blocks.Add(newBlock);

                //save changes
                await _blocksDbContext.SaveChangesAsync();

                //commit changes
                await block_transaction.CommitAsync();
            }
            catch (Exception ex)
            {
                await block_transaction.RollbackAsync();
                throw new Exception($"Could not add block : {ex.Message}");
            }
        }


        public async Task<transaction_response> BroadCastVote(int numNodes, VoteBroadcastRequest req)
        { //return true if all nodes received a transaction, false if any node failed to receive it --fix later

            //store the responses from nodes
            transaction_response transaction_response = new transaction_response
            {
                messages = new List<string>(),
                status = new List<bool>()
            };

            


            for (int i = 1; i <= numNodes; i++)
            {
                try
                {   //node{i} represents the internal docker node
                    //do not send the actual vote in this request -> it will fail
                    using HttpResponseMessage response = await _httpClient.PostAsJsonAsync($"http://node{i}:8080/api/Blockchain/handle_vote_request", req);
                    
                    //FIX we should check if the node responded successfully on each broadcast
                    //response.EnsureSuccessStatusCode();<- adding this breaks the code
                    string responseBody = await response.Content.ReadAsStringAsync();

                    transaction_response.messages.Add(responseBody);
                    transaction_response.status.Add(true);      
                }
                catch (HttpRequestException e)
                {
                    transaction_response.messages.Add($"Node {i}: {e.Message}");
                    transaction_response.status.Add(false);
                }
            }

            return transaction_response;
        }


        //check that all the nodes are valid and that all the nodes matches eachother
        public async Task<bool> IsBlockchainValid(int numNodes) {
            try {
                //check if all nodes have valid chains
                var _IsAllNodeChainValid = await IsAllNodeChainsValid(numNodes);
                if (!_IsAllNodeChainValid) return false;

                //check that all nodes have the same chain
                var _IsAllNodesEqual = await IsAllNodesEqual(numNodes);
                if (!_IsAllNodesEqual) return false;

                return true;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
            
        }





         //===========================================VALIDATE BLOCKCHAIN METHODS===================================================
        public async Task<bool> IsAllNodeChainsValid(int numNodes) {
            try {
                var tasks = Enumerable
                    .Range(1, numNodes)
                    .Select(isNodechainValid_id);

                bool[] results = await Task.WhenAll(tasks);

                if(results.Length <= 0) throw new Exception("No nodes found to validate");

                return results.All(x => x == true);
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }


        //compares all the nodes in the blockchain network and see if they are all equal, returns true if all nodes have the same chain, false if any node has a different chain
        public async Task<bool> IsAllNodesEqual(int numNodes)
        {
            try {
                var tasks = Enumerable
                    .Range(1, numNodes)
                    .Select(GetNodeChain_id);

                List<Block>[] blockchains = await Task.WhenAll(tasks);

                if (blockchains.Length == 0) throw new Exception("No blockchains found to compare");

                var referenceChain = blockchains[0];

                foreach (var chain in blockchains.Skip(1))
                {
                    // Different number of blocks
                    if (chain.Count != referenceChain.Count)
                        return false;

                    for (int i = 0; i < referenceChain.Count; i++)
                    {
                        var referenceBlock = referenceChain[i];
                        var currentBlock = chain[i];

                        if (referenceBlock.hash != currentBlock.hash)
                        {
                            return false;
                        }
                    }
                }

                return true;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }




        //makes a request to all the nodes to check if their individual chains are valid
        public async Task<bool> isNodechainValid_id(int node_id)
        {
            try
            {
                using var request = new HttpRequestMessage(HttpMethod.Get, $"http://node{node_id}:8080/api/Blockchain/isNodechainValid");

                var response = await _httpClient.SendAsync(request);
                var responseBody = await response.Content.ReadFromJsonAsync<bool>();

                //if (!response.IsSuccessStatusCode) return false;

                return responseBody;

            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return false;
            }
        }


        //checks the current node's blockchain and see if it is valid
        public async Task<bool> IsNodeChainValid()
        {
            try {
                var blockchain = await _blocksDbContext.blocks
                    .OrderBy(b => b.index)
                    .ToArrayAsync();

                for (int i = 0; i < blockchain.Length; i++)
                {
                    var current = blockchain[i];

                    // Recalculate the hash
                    var calculatedHash = current.CalculateHash();

                    var rawData = $"{current.index}-{current.timestamp.ToUniversalTime():O}-{current.Data}-{current.previousHash}";

                    if (current.hash != calculatedHash) return false;

                    // Check link to previous block
                    if (i > 0)
                    {
                        var previous = blockchain[i - 1];

                        if (current.previousHash != previous.hash) return false;
                    }
                }

                return true;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }


        //used by main sever to get the blockchain from a specific node
        public async Task<List<Block>> GetNodeChain_id(int node_id)
        {
            try
            {
                using var request = new HttpRequestMessage(HttpMethod.Get, $"http://node{node_id}:8080/api/Blockchain/GetNodeChain");

                var response = await _httpClient.SendAsync(request);
                var responseBody = await response.Content.ReadAsStringAsync();

                //if (!response.IsSuccessStatusCode) return null;

                var blockchain = await response.Content.ReadFromJsonAsync<List<Block>>();

                return blockchain ?? new List<Block>();
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }

        }


        //used on curent node to get the blockchain from the database 
        public async Task<List<Block>> GetNodeChain()
        {
            try {
                var blockchain = await _blocksDbContext.blocks
                     .OrderBy(b => b.index)
                     .ToListAsync();

                return blockchain;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }


        }






    }

}
