using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.WebUtilities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using Npgsql;
using System.Security.Claims;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;

namespace votingBackend3.Services
{
    public class VoteService
    {
        private readonly VotingDbContext _votingDbContext;
        private readonly BlocksDbContext _blocksDbContext;
        private readonly BlockchainService _blockchainService;
        private readonly IConfiguration _configuration;

        private readonly int TokenLifetime = 0;//number of minutes before the token expires

        public VoteService(BlockchainService blockchainService, VotingDbContext votingDbContext, BlocksDbContext blocksDbContext, IConfiguration configuration)
        {
            _blockchainService = blockchainService;
            _configuration = configuration;
            _votingDbContext = votingDbContext;
            _blocksDbContext = blocksDbContext;
            TokenLifetime = _configuration.GetValue<int>("TokenLifetime"!);
        }

        //return true if the user has already voted in this election, false if they still need to vote, error when not registered for the election
        public async Task<bool> HasUserVoted(Guid voter_id, Guid election_id)
        {
            var electionVoter = await _votingDbContext.election_voter
                .FirstOrDefaultAsync(ev =>
                    ev.voter_id == voter_id &&
                    ev.election_id == election_id);

            if (electionVoter == null)
            {
                throw new Exception("Voter is not registered for this election");
            }

            if(electionVoter.has_voted == true)
            {
                throw new Exception("Voter has already voted in this election");
            }

            return false;
        }


        public async Task<VoteTokenDto> CreateVoteToken(Guid voter_id, Guid election_id) {
            try {
                //check if election is live
                Election election = await _votingDbContext.election.FindAsync(election_id);

                if (election == null) throw new Exception("Election not found");
                if (!election.isActive) throw new Exception("This Election is currently not live");


                //check if the user has already voted in this election, if so, do not allow them to vote again
                bool hasvoted = await HasUserVoted(voter_id, election_id);
                if (hasvoted) throw new Exception("Voter has already voted in this election");

                //create the token
                var token = RandomNumberGenerator.GetBytes(32);

                //refers to token used to vote with
                VoteTokenDto voteToken = new VoteTokenDto
                {
                    token_id = Guid.NewGuid(),
                    Token = WebEncoders.Base64UrlEncode(token),
                    IssuedAt = DateTime.UtcNow,
                };

                //refers to database table where the token is saved and hashed for security reasons, so that the token cannot be reverse engineered
                VotingToken votingToken = new VotingToken
                {
                    token_id = voteToken.token_id,
                    TokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(voteToken.Token))),
                    election_id = election_id,//from attributes
                    IssuedAt = voteToken.IssuedAt,
                    isUsed = false
                };


                //save to database
                _votingDbContext.voting_token.Add(votingToken);
                await _votingDbContext.SaveChangesAsync();

                return voteToken;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
            
        }



        //check if the parameters are correct and exist. Also check if the token exist in the database under votingToken table
        public async Task<VotingToken> VerifyVoteRequest(vote_request request) {
            try {
                if (request == null) throw new Exception("Invalid vote data");
                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid vote format");

                //check for null values
                if (request.voteToken == null || request.voteToken.Equals(string.Empty)) throw new Exception("Invalid vote token");
                if (request.election_id == Guid.Empty) throw new Exception("Invalid Election ID");
                if (request.party_id == Guid.Empty) throw new Exception("Invalid Party ID");

                //verify token
                //check if voteToken exist in database
                var token = await _votingDbContext.voting_token.FindAsync(request.voteToken.token_id);

                //no token found
                if (token == null) throw new Exception("Voting token not found");

                //token is expired
                if (DateTime.UtcNow - token.IssuedAt > TimeSpan.FromMinutes(TokenLifetime)) throw new Exception("Voting token expired");

                //token was already used
                if (token.isUsed == true) throw new Exception("Voting token already used");


                //check if token form request matches hashed token in database
                string tokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(request.voteToken.Token)));

                if (token.TokenHash.Equals(tokenHash) == false) throw new Exception("Voting token hash does not match");


                //valid token
                return token;

            }
            catch (Exception ex)
            {
                throw new Exception($"Could not verify the vote request: {ex.Message}");
            }
            
        }

        //check if the vote was saved to the database and not yet added to the blockchain
        public async Task<bool> ValidateBroadCastRequest(VoteBroadcastRequest request, string hash)
        {
            try
            {
                //does election, party and vote exist in database
                var exists = await _votingDbContext.election
                    .Where(e => e.election_id == request.election_id)
                    .Select(e => new
                    {
                        ElectionExists = true,

                        PartyExists = _votingDbContext.party
                            .Any(p => p.party_id == request.party_id),

                        VoteExists = _votingDbContext.vote
                            .Any(v => v.vote_id == request.vote_id)
                    })
                    .FirstOrDefaultAsync();

                if (exists == null) throw new Exception("Election does not exist.");

                if (!exists.PartyExists) throw new Exception("Party does not exist.");

                if (!exists.VoteExists) throw new Exception("Vote does not exist.");


                //check if block already exist
                Block block_exist = await _blocksDbContext.blocks.FirstOrDefaultAsync(b => b.hash == hash);
                if (block_exist != null) throw new Exception("Vote already added to chain");

                return true;

            }
            catch (Exception ex)
            {
                throw new Exception($"Could not validate the vote: {ex.Message}");
            }

        }



        //this checks if the vote exist in the database under vote table, this is used to check if the vote was already submitted
        public async Task<Vote> GetVoteByToken(Guid token_id) {
            try {
                Vote vote = _votingDbContext.vote.FirstOrDefault(v => v.token_id == token_id);

                if (vote == null) throw new Exception("Vote not found");
                if(vote.votingToken.isUsed == false) throw new Exception("Token does not belong to a submitted vote");//NOTE This isnt that important

                return vote;
            }
            catch (Exception ex)
            {
                throw new Exception($"Could not find vote by token: {ex.Message}");
            }
            
        }



        //save the vote to the databse and mark the token as used
        public async Task<Vote> SaveVote(Guid voter_id, vote_request request, VotingToken token) {
            
            var vote_transaction = await _votingDbContext.Database.BeginTransactionAsync();

            try {
                if(request == null) throw new Exception("Invalid vote_request");
                if (token == null) throw new Exception("Invalid token");

                //update user voted status in election_voter table
                var electionVoter = await _votingDbContext.election_voter
                .FirstOrDefaultAsync(ev =>
                    ev.voter_id == voter_id &&
                    ev.election_id == request.election_id);

                //check if valid electionVoter was found
                if (electionVoter == null) throw new Exception("Voter is not registered for this election");

                electionVoter.has_voted = true;

                //add vote to ledger
                Vote vote = new Vote
                {
                    token_id = token.token_id,
                    votingToken = token,
                    election_id = request.election_id,
                    party_id = request.party_id,
                    created_at = DateTime.UtcNow,
                };

                //add to database
                var voteEntry = await _votingDbContext.vote.AddAsync(vote);

                //update the token status so it cannot be used more than once
                int rowsUpdated = await _votingDbContext.voting_token
                    .Where(t => t.token_id == token.token_id)
                    .ExecuteUpdateAsync(t => t.SetProperty(t => t.isUsed, true));

                // Token wasn't found, or something unexpected happened
                if (rowsUpdated < 1) throw new Exception("Failed to mark token as used");

                //more than one token was found with the same token_id, this should never happen
                if (rowsUpdated > 1) throw new Exception("Duplicate voting tokens found, rejected request");

                //save changes
                await _votingDbContext.SaveChangesAsync();

                //commit changes
                await vote_transaction.CommitAsync();

                return vote;
            }
            catch (DbUpdateException ex) when (ex.InnerException is PostgresException pgEx && pgEx.SqlState == "23505")
            {
                await vote_transaction.RollbackAsync();
                throw new Exception("Vote already exists");
            }
            catch (Exception ex)
            {
                await vote_transaction.RollbackAsync();
                throw new Exception($"Could not save vote: {ex.Message}");
            }
            
        }




        public async Task<vote_receipt> VerifyVote_Receipt(Guid vote_id) {
            var vote_data = await _votingDbContext.vote
                .Include(v=> v.Election)
                .FirstOrDefaultAsync(v => v.vote_id == vote_id);

            if (vote_data == null) throw new Exception("Vote not found");


            //create a vote receipt
            vote_receipt vote_receipt = new vote_receipt
            {
                election_id = vote_data.election_id,
                hasVoted = true,//if the vote is found, it means the user has voted. Should always be true
                voted_at = vote_data.created_at
            };

            return vote_receipt;
        }




    }
}

