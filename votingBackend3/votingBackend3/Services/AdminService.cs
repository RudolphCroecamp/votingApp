using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.Linq.Expressions;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;

namespace votingBackend3.Services
{
    public class AdminService
    {

        private readonly VotingDbContext _votingDbContext;
        private readonly BlockchainService _blockchainService;
        private readonly VoteService _voteService;

        public AdminService(BlockchainService blockchainService, VotingDbContext votingDbContext, VoteService voteService)
        {
            _blockchainService = blockchainService;
            _votingDbContext = votingDbContext;
            _voteService = voteService;
        }



        public async Task<List<Election>> GetElections() {
            //get all elections from db
            List<Election> elections = await _votingDbContext.election.ToListAsync();
            return elections;
        }

        public async Task<List<Party>> GetParties()
        {
            //get all elections from db
            List<Party> parties = await _votingDbContext.party.ToListAsync();
            return parties;
        }


        public async Task<bool> CreateElection(new_election request) {
            try
            {
                if (request == null) return false;

                if (request.GetType() != typeof(new_election)) return false;

                Election new_election = new Election
                {
                    election_id = Guid.NewGuid(),
                    election_name = request.election_name,
                    election_type = request.election_type,
                    start_date = request.start_date,
                    end_date = request.end_date,
                    isActive = request.start_date <= DateOnly.FromDateTime(DateTime.Now) && request.end_date >= DateOnly.FromDateTime(DateTime.Now)
                };

                await _votingDbContext.election.AddAsync(new_election);
                await _votingDbContext.SaveChangesAsync();

                return true;
            }
            catch {

                return false;
            } 
        }



        public async Task<bool> CreateParty(new_party request)
        {
            try
            {
                if (request == null) return false;

                if (request.GetType() != typeof(new_party)) return false;


                Party new_party = new Party
                {
                    party_id = Guid.NewGuid(),
                    party_name = request.party_name,
                    party_description = request.party_description,
                    status = false
                };


                await _votingDbContext.party.AddAsync(new_party);
                await _votingDbContext.SaveChangesAsync();

                return true;

            }
            catch {
                return false;
            }

            
        }











    }
}
