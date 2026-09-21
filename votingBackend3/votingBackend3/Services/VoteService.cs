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

            //check if election is live
            Election election = await _votingDbContext.election.FindAsync(request.election_id);

            if (election == null)//|| !election.isActive
            {
                return null;
            }

            //create the token
            var token = RandomNumberGenerator.GetBytes(32);

            //refers to token used toi vote with
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



        //verify the vote to the database -> did we create the key
        public async Task<VotingToken> VerifyVoteRequest(vote_request request) {

            if(request == null) return null;

            if(request.GetType() != typeof(vote_request)) return null;

            //check for null values
            if(request.voteToken == null) return null;
            if (request.election_id == Guid.Empty) return null;
            if (request.party_id == Guid.Empty) return null;
            if(request.election_type == null || request.election_type == string.Empty) return null;

            //verify token
            VotingToken token = await VerifyToken(request.voteToken);

            //no token found
            if (token == null) return null;


            //valid token
            return token;
        }


        //verify the vote token to the database -> did we create the key and has it been used
        public async Task<VotingToken> VerifyToken(VoteToken request)
        {
            //check if voteToken exist in database
            var token = await _votingDbContext.voting_token.FindAsync(request.token_id);

            //no token found
            if (token == null) return null;

            //token is expired
            if (DateTime.UtcNow - token.IssuedAt > TimeSpan.FromMinutes(TokenLifetime)) return null;

            //token was already used
            if (token.isUsed == true) return null;


            //check if token form request matches hashed token in database
            string tokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(request.Token)));

            if (token.TokenHash.Equals(tokenHash) == false) return null;

            //valid token
            return token;
        }


        public async Task<Vote> VerifyVote(vote_request request) { 
            Vote vote = _votingDbContext.vote.FirstOrDefault(v => v.token_id == request.voteToken.token_id);

            if(vote == null) return null;

            return vote;
        }






        public async Task<vote_response> SaveVote(vote_request request, VotingToken token) {
            if(request == null || token == null) return null;   

            //add vote_request to ledger
            Vote vote = new Vote
            {
                token_id = token.token_id,
                votingToken = token,
                election_id = request.election_id,
                party_id = request.party_id,
                election_type = request.election_type,
            };


            //save vote to db
            var vote_transaction = await _votingDbContext.Database.BeginTransactionAsync();

            try
            {
                //add to database
                _votingDbContext.vote.Add(vote);

                //save changes
                await _votingDbContext.SaveChangesAsync();

                //commit changes
                await vote_transaction.CommitAsync();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return null;
            }

            vote_response response = new vote_response
            {
                token_receipt = token.token_id,
                message = "Vote saved successfully"
            };


            return response;
        }



    }
}

