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



        public async Task<List<Election>> GetElections()
        {
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


        public async Task<Election> CreateElection(new_electionDto request)
        {
            try
            {
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(new_electionDto)) throw new Exception("Invalid request format");

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
            catch (Exception ex)
            {
                throw new Exception($"Could not create the Election. error: {ex.Message}");
            }
        }



        public async Task<Party> CreateParty(new_partyDto request)
        {
            try
            {
                if (request == null) throw new Exception("Invalid request data");

                if (request.GetType() != typeof(new_partyDto)) throw new Exception("Invalid request format");


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

        //update election status and party status
        public async Task<Election> updateElectionStatus(updateStatusDto data)
        {//true if updated fasle if failed
            try
            {
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

        public async Task<Party> updatePartyStatus(updateStatusDto data)
        {
            try
            {
                if (data.ID.GetType() != typeof(Guid) || data.ID == Guid.Empty) throw new Exception("Invalid ID");
                if (data.status.GetType() != typeof(bool)) throw new Exception("Invalid status");


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



        //update election information
        public async Task<Election> updateElectionInformation(Guid election_id, updateElectionDto request)
        {
            try
            {
                var election = await _votingDbContext.election.FindAsync(election_id);

                if (election == null) throw new Exception("Election not found");

                //update
                election.election_name = request.election_name;
                election.election_type = request.election_type;
                election.start_date = request.start_date;
                election.end_date = request.end_date;
                election.isActive = request.isActive;

                //save changes
                await _votingDbContext.SaveChangesAsync();
                return election;
            }
            catch (Exception ex)
            {
                throw new Exception($"Could not update the Election: {ex.Message}");
            }
        }

        //update party information
        public async Task<Party> updatePartyInformation(Guid party_id, updatePartyDto request)
        {
            try
            {
                var party = await _votingDbContext.party.FindAsync(party_id);

                if (party == null) throw new Exception("Party not found");

                //update
                party.party_name = request.party_name;
                party.party_description = request.party_description;
                party.status = request.status;

                //save changes
                await _votingDbContext.SaveChangesAsync();
                return party;
            }
            catch (Exception ex)
            {
                throw new Exception($"Could not update the Party: {ex.Message}");
            }
        }



        //calculate winners and results of the election

        public async Task<List<ElectionResultsDTO>> GetElectionResults(Guid election_id)
        {
            var totalVotes = await _votingDbContext.vote.CountAsync(v => v.election_id == election_id);

            var electionResults = await _votingDbContext.vote
                .Where(v => v.election_id == election_id)
                .GroupBy(v => new { v.party_id, v.Party.party_name })
                .Select(g => new ElectionResultsDTO
                {
                    party_id = g.Key.party_id,
                    party_name = g.Key.party_name,
                    votes = g.Count(),
                    percentage = totalVotes == 0 ? 0 : (double)g.Count() / totalVotes * 100
                })
                .ToListAsync();

            if (electionResults == null) throw new Exception("Election results not found");

            return electionResults;

        }

        //public async Task<List<ElectionResultsDTO>> GetAllElectionResults()
        //{
        //    var totalVotes = await _votingDbContext.vote.CountAsync();
        //    var electionResults = await _votingDbContext.vote
        //        .GroupBy(v => new { v.election_id, v.Party.party_name })
        //        .Select(g => new ElectionResultsDTO
        //        {
        //            party_id = g.Key.election_id,
        //            party_name = g.Key.party_name,
        //            votes = g.Count(),
        //            percentage = totalVotes == 0 ? 0 : (double)g.Count() / totalVotes * 100
        //        })
        //        .ToListAsync();
        //    if (electionResults == null) throw new Exception("Election results not found");
        //    return electionResults;

        //}


        public async Task<ElectionResultsDTO> GetElectionWinner(Guid election_id)
        {
            var totalVotes = await _votingDbContext.vote.CountAsync(v => v.election_id == election_id);
            var electionResults = await _votingDbContext.vote
                .Where(v => v.election_id == election_id)
                .GroupBy(v => new { v.party_id, v.Party.party_name })
                .Select(g => new ElectionResultsDTO
                {
                    party_id = g.Key.party_id,
                    party_name = g.Key.party_name,
                    votes = g.Count(),
                    percentage = totalVotes == 0 ? 0 : (double)g.Count() / totalVotes * 100
                })
                .OrderByDescending(r => r.votes)
                .ToListAsync();

            if (electionResults == null || electionResults.Count == 0) throw new Exception("Election results not found");

            return electionResults.First();
        }







    }
}
