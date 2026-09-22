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
            Console.WriteLine("!!!!!1add genesis block function called!!!!!");


            //check ofr blocks in database
            var hasBlocks = await _blocksDbContext.blocks.AnyAsync();
            if (hasBlocks) return;

            Block genesisBlock = new Block(0, DateTime.UtcNow, "genesis block", "");

            var block_transaction = await _blocksDbContext.Database.BeginTransactionAsync();
            try
            {
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

                Console.WriteLine("=================================");
                Console.WriteLine("DATABASE ERROR");
                Console.WriteLine(ex.ToString());
                Console.WriteLine("=================================");

            }
        }

        public async Task<Block> GetLatestBlock()
        {
            return await _blocksDbContext.blocks
                .OrderByDescending(b => b.index)
                .FirstAsync();
        }

        public async Task addBlock(Block newBlock)
        {
            newBlock.previousHash = (await GetLatestBlock()).hash;

            var block_transaction = await _blocksDbContext.Database.BeginTransactionAsync();
            try
            {
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

                Console.WriteLine("=================================");
                Console.WriteLine("DATABASE ERROR");
                Console.WriteLine(ex.ToString());
                Console.WriteLine("=================================");

            }
        }


        //public async Task<bool> isChainValid()
        //{
        //    for (int i = 1; i < chain.Count; i++)
        //    {
        //        Block currentBlock = chain[i];
        //        Block previousBlock = chain[i - 1];

        //        if (currentBlock.hash != currentBlock.CalculateHash())
        //        {
        //            return false;
        //        }
        //        if (currentBlock.previousHash != previousBlock.hash)
        //        {
        //            return false;
        //        }
        //    }

        //    return true;

        //}


        public async Task<transaction_response> BroadCastVote(int numNodes, vote_request request )
        { //return true if all nodes received a transaction, false if any node failed to receive it --fix later

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
                    response.EnsureSuccessStatusCode();
                    string responseBody = await response.Content.ReadAsStringAsync();

                    //Console.WriteLine(responseBody);

                    transaction_response.messages.Add(responseBody);
                    transaction_response.status.Add(true);      
                }
                catch (HttpRequestException e)
                {
                    Console.WriteLine("\nException Caught!");
                    Console.WriteLine("Message :{0} ", e.Message);
                    //transaction_response.messages.Add($"Message :{0} , {e.Message}");
                    transaction_response.messages.Add($"Node {i}: {e.Message}");
                    transaction_response.status.Add(false);
                }
            }


            return transaction_response;
        }



        //public async Task<List<List<Block>>> GetChains()
        //{
        //    int numberOfNodes = 4;
        //    List<List<Block>> chains = new List<List<Block>>();

        //    for (int i = 1; i <= numberOfNodes; i++)
        //    {
        //        chains.Add(await GetChainFromNode(i));
        //    }

        //    return chains;
        //}





        //private async Task<List<Block>> GetChainFromNode(int nodeId)
        //{
        //    List<Block> chain = new List<Block>();

        //    try
        //    {
        //        using HttpResponseMessage response = await _httpClient.GetAsync($"http://back{nodeId}:8080/api/ping");
        //        response.EnsureSuccessStatusCode();
        //        string responseBody = await response.Content.ReadAsStringAsync();

        //        chain.AddRange(System.Text.Json.JsonSerializer.Deserialize<List<Block>>(responseBody));
        //    }
        //    catch (HttpRequestException e)
        //    {
        //        Console.WriteLine("\nException Caught!");
        //        Console.WriteLine("Message :{0} ", e.Message);
        //    }

        //    return chain;

        //}





    }

}
