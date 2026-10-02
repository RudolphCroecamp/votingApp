using Microsoft.EntityFrameworkCore;
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

                //create genesis block
                Block genesisBlock = new Block(0, DateTime.UtcNow, "genesis block", "");

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
                throw new Exception(ex.Message);
            }
        }


        public async Task<transaction_response> BroadCastVote(int numNodes, vote_request request )
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
                {
                    using HttpResponseMessage response = await _httpClient.PostAsJsonAsync($"http://node{i}:8080/api/Blockchain/handle_vote_request", request);//node{i} represents the internal docker node
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

        //makes a request toall the nodes to check if their chains are valid
        public async Task<transaction_response> IsBlockchainValid(int numNodes)
        {
            var transactionResponse = new transaction_response
            {
                messages = new List<string>(),
                status = new List<bool>()
            };

            for (int i = 1; i <= numNodes; i++)
            {
                try
                {
                    using var request = new HttpRequestMessage(HttpMethod.Get,$"http://node{i}:8080/api/Blockchain/isNodechainValid");

                    var response = await _httpClient.SendAsync(request);
                    var responseBody = await response.Content.ReadAsStringAsync();

                    if (!response.IsSuccessStatusCode)
                    {
                        transactionResponse.messages.Add($"Node {i}: HTTP {(int)response.StatusCode} - {responseBody}");

                        transactionResponse.status.Add(false);
                        continue;
                    }

                    transactionResponse.messages.Add($"Node {i}: {responseBody}");

                    transactionResponse.status.Add(true);
                }
                catch (Exception ex)
                {
                    transactionResponse.messages.Add($"Node {i}: {ex.Message}");

                    transactionResponse.status.Add(false);
                }
            }

            return transactionResponse;
        }

        //testing purposes only, not used in production

        //public async Task<string> GetNodeChain_id(int node) {
        //    try
        //    {
        //        using var request = new HttpRequestMessage(HttpMethod.Get, $"http://node{node}:8080/api/Blockchain/GetNodeChain");

        //        var response = await _httpClient.SendAsync(request);
        //        var responseBody = await response.Content.ReadAsStringAsync();

        //        //if (!response.IsSuccessStatusCode) return null;

        //        return responseBody;
        //    }
        //    catch (Exception ex)
        //    {
        //        throw new Exception(ex.Message);
        //    }

        //}

        //public async Task<List<Block>> GetNodeChain()
        //{
        //    var blockchain = await _blocksDbContext.blocks
        //         .OrderBy(b => b.index)
        //         .ToListAsync();

        //    return blockchain;

        //}


        //checks the current node's blockchain and see if it is valid
        public async Task<bool> IsNodeChainValid()
        {
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

    }

}
