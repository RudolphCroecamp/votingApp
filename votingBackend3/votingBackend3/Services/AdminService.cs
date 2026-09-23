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


        public async Task<Election> CreateElection(new_election request) {
            try
            {
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(new_election)) throw new Exception("Invalid request format");

                Election new_election = new Election
                {
                    election_id = Guid.NewGuid(),
                    election_name = request.election_name,
                    election_type = request.election_type,
                    start_date = request.start_date,
                    end_date = request.end_date,
                    isActive = request.start_date <= DateOnly.FromDateTime(DateTime.Now) && request.end_date >= DateOnly.FromDateTime(DateTime.Now)
                };

                //add and save changes
                await _votingDbContext.election.AddAsync(new_election);
                await _votingDbContext.SaveChangesAsync();

                return new_election;
            }
            catch(Exception ex) 
            {
                throw new Exception($"Could not create the Election. error: {ex.Message}");
            } 
        }



        public async Task<Party> CreateParty(new_party request)
        {
            try
            {
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(new_party)) throw new Exception("Invalid request format");


                Party new_party = new Party
                {
                    party_id = Guid.NewGuid(),
                    party_name = request.party_name,
                    party_description = request.party_description,
                    status = false
                };

                //add and save changes
                await _votingDbContext.party.AddAsync(new_party);
                await _votingDbContext.SaveChangesAsync();

                return new_party;

            }
            catch (Exception ex)
            {
                throw new Exception($"Could not create the Party. error: {ex.Message}");
            }

            
        }


        public async Task<Election> updateElectionStatus(updateStatus data) {//true if updated fasle if failed
            try {
                if (data.ID.GetType() != typeof(Guid) || data.ID == Guid.Empty) throw new Exception("Invalid ID");
                if (data.status.GetType() != typeof(bool)) throw new Exception("Invalid status");

                var election = await _votingDbContext.election.FindAsync(data.ID);

                if (election == null) throw new Exception("Election not found");

                //update
                election.isActive = data.status;

                //save changes
                await _votingDbContext.SaveChangesAsync();

                return election;
            }
            catch (Exception ex)
            {
                throw new Exception($"Could not update the Election. error: {ex.Message}");
            }
        }

        public async Task<Party> updatePartyStatus(updateStatus data)
        {
            try
            { 
                if(data.ID.GetType() != typeof(Guid) || data.ID == Guid.Empty) throw new Exception("Invalid ID");
                if(data.status.GetType() != typeof(bool)) throw new Exception("Invalid status");


                var party = await _votingDbContext.party.FindAsync(data.ID);

                if (party == null) throw new Exception("Party not found");

                //update
                party.status = data.status;

                //save changes
                await _votingDbContext.SaveChangesAsync();

                return party;
            }
            catch (Exception ex)
            {
                throw new Exception($"Could not update the Party. error: {ex.Message}");
            }
        }

    }
}
