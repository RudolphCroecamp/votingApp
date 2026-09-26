using Microsoft.AspNetCore.WebUtilities;
using Microsoft.IdentityModel.Tokens;
using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;
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
        private readonly BlockchainService _blockchainService;

        private readonly int TokenLifetime = 10;//number of minutes before the token expires

        public VoteService(BlockchainService blockchainService, VotingDbContext votingDbContext)
        {
            _blockchainService = blockchainService;
            _votingDbContext = votingDbContext;
        }



        public async Task<VoteToken> CreateVoteToken(VoteTokenRequest request) {
            try {
                //check if election is live
                Election election = await _votingDbContext.election.FindAsync(request.election_id);

                //FIX: add the condition -> || !election.isActive
                if (election == null)
                {
                    throw new Exception("This Election is currently not live");
                }

                //create the token
                var token = RandomNumberGenerator.GetBytes(32);

                //refers to token used to vote with
                VoteToken voteToken = new VoteToken
                {
                    token_id = Guid.NewGuid(),
                    Token = WebEncoders.Base64UrlEncode(token),
                    IssuedAt = DateTime.UtcNow,
                };

                //refers to database table
                VotingToken votingToken = new VotingToken
                {
                    token_id = voteToken.token_id,
                    TokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(voteToken.Token))),
                    election_id = request.election_id,
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



        //verify the vote to the database -> did we create the key
        public async Task<VotingToken> VerifyVoteRequest(vote_request request) {
            try {
                if (request == null) throw new Exception("Invalid vote data");

                if (request.GetType() != typeof(vote_request)) throw new Exception("Invalid vote format");

                //check for null values
                if (request.voteToken == null || request.voteToken.Equals(string.Empty)) throw new Exception("Invalid vote token");
                if (request.election_id == Guid.Empty) throw new Exception("Invalid Election ID");
                if (request.party_id == Guid.Empty) throw new Exception("Invalid Party ID");
                if (request.election_type == null || request.election_type == string.Empty) throw new Exception("Invalid vote token");

                //verify token
                VotingToken token = await VerifyToken(request.voteToken);

                //no token found
                if (token == null) throw new Exception("Vote token not found");


                //valid token
                return token;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
            
        }


        //verify the vote token to the database -> did we create the key and has it been used
        public async Task<VotingToken> VerifyToken(VoteToken request)
        {
            try {
                //check if voteToken exist in database
                var token = await _votingDbContext.voting_token.FindAsync(request.token_id);

                //no token found
                if (token == null) throw new Exception("Voting token not found");

                //token is expired
                if (DateTime.UtcNow - token.IssuedAt > TimeSpan.FromMinutes(TokenLifetime)) throw new Exception("Voting token expired");

                //token was already used
                if (token.isUsed == true) throw new Exception("Voting token already used");


                //check if token form request matches hashed token in database
                string tokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(request.Token)));

                if (token.TokenHash.Equals(tokenHash) == false) throw new Exception("Voting token does not match");

                //valid token
                return token;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
            
        }

        //TODO what is this even for?
        public async Task<Vote> VerifyVote(vote_request request) {
            try {
                Vote vote = _votingDbContext.vote.FirstOrDefault(v => v.token_id == request.voteToken.token_id);

                if (vote == null) throw new Exception("Vote not found");

                return vote;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
            
        }






        public async Task<vote_response> SaveVote(vote_request request, VotingToken token) {
            
            var vote_transaction = await _votingDbContext.Database.BeginTransactionAsync();

            try {
                if(request == null) throw new Exception("Invalid vote_request");
                if (token == null) throw new Exception("Invalid token");

                //add vote_request to ledger
                Vote vote = new Vote
                {
                    token_id = token.token_id,
                    votingToken = token,
                    election_id = request.election_id,
                    party_id = request.party_id,
                    election_type = request.election_type,
                };

                //add to database
                _votingDbContext.vote.Add(vote);

                //save changes
                await _votingDbContext.SaveChangesAsync();

                //commit changes
                await vote_transaction.CommitAsync();

                vote_response response = new vote_response
                {
                    token_receipt = token.token_id,
                    message = "Vote saved successfully"
                };


                return response;
            }
            catch (Exception ex)
            {
                await vote_transaction.RollbackAsync();
                throw new Exception(ex.Message);
            }
            
        }



    }
}

