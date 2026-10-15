using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
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

                if (request.start_date > request.end_date) throw new Exception("Start date cannot be after end date.");

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
            if (request == null) throw new ArgumentNullException(nameof(request), "Invalid request data.");
            if (request.GetType() != typeof(new_electionDto)) throw new Exception("Invalid request format");

            //Checks to see if party name is unique
            var partyExists = await _votingDbContext.party.AnyAsync(p => p.name == request.name);
            if (partyExists) throw new InvalidOperationException("Party already exists.");

            var newParty = new Party
            {
                party_id = Guid.NewGuid(),
                name = request.name,
                abbreviation = request.abbreviation,
                description = request.description,
                allowed = request.allowed,
                created_at = DateTime.UtcNow
            };

            await _votingDbContext.party.AddAsync(newParty);
            await _votingDbContext.SaveChangesAsync();

            return newParty;
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
                party.allowed = data.status;

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
                //Checks Date
                if (request.start_date > request.end_date) throw new ArgumentException("Start date cannot be after the end date.");

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
                party.name = request.name;
                party.description = request.description;
                party.abbreviation = request.abbreviation;
                party.allowed = request.allowed;

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
                .GroupBy(v => new { v.party_id, v.Party.name })
                .Select(g => new ElectionResultsDTO
                {
                    party_id = g.Key.party_id,
                    party_name = g.Key.name,
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
                .GroupBy(v => new { v.party_id, v.Party.name })
                .Select(g => new ElectionResultsDTO
                {
                    party_id = g.Key.party_id,
                    party_name = g.Key.name,
                    votes = g.Count(),
                    percentage = totalVotes == 0 ? 0 : (double)g.Count() / totalVotes * 100
                })
                .OrderByDescending(r => r.votes)
                .ToListAsync();

            if (electionResults == null || electionResults.Count == 0) throw new Exception("Election results not found");

            return electionResults.First();
        }


        //allow the specified party to partisipate in an election
        public async Task<ElectionParty> AssignPartyToElection(
    AssignPartyToElectionDto request)
        {
            var election = await _votingDbContext.election.FindAsync(request.election_id);

            if (election == null)throw new Exception("Election not found.");

            var party = await _votingDbContext.party.FindAsync(request.party_id);

            if (party == null) throw new Exception("Party not found.");

            if (!party.allowed)throw new Exception("This party is disabled.");

            var existingAssignment = await _votingDbContext.election_party
                .AnyAsync(ep =>
                    ep.election_id == request.election_id &&
                    ep.party_id == request.party_id);

            if (existingAssignment) throw new Exception("This party is already assigned to this election.");

            var assignment = new ElectionParty
            {
                election_id = request.election_id,
                party_id = request.party_id
            };

            _votingDbContext.election_party.Add(assignment);

            await _votingDbContext.SaveChangesAsync();

            return assignment;
        }


        //get all the election parties
        public async Task<List<Party>> GetElectionParties(Guid election_id)
        {
            var electionExists = await _votingDbContext.election.AnyAsync(e => e.election_id == election_id);

            if (!electionExists)throw new KeyNotFoundException("Election not found.");

            var parties = await _votingDbContext.party
                .Where(p => _votingDbContext.election_party.Any(ep =>
                    ep.party_id == p.party_id &&
                    ep.election_id == election_id))
                .ToListAsync();

            return parties;
        }




        //manage voters

        //get all the users registered in a specified election
        public async Task<List<ElectionVoterDto>> GetElectionVoters(Guid election_id, int limit = 10, int skip = 0)
        {
            if (limit < 1 || limit > 100) throw new ArgumentOutOfRangeException(nameof(limit), "Limit must be between 1 and 100.");

            if (skip < 0) throw new ArgumentOutOfRangeException(nameof(skip), "Skip cannot be negative.");

            return await (
                from ev in _votingDbContext.election_voter
                join a in _votingDbContext.auth
                    on ev.voter_id equals a.voter_id
                where ev.election_id == election_id
                      && a.role == "Voter"
                orderby ev.voter_id
                select new ElectionVoterDto
                {
                    election_id = ev.election_id,
                    voter_id = ev.voter_id,
                    voted = ev.has_voted,
                    status = ev.status
                }
            )
            .Skip(skip)
            .Take(limit)
            .ToListAsync();
        }


        
        public async Task<VoterListDTO> GetAllVoters(int page = 1, int limit = 10)
        {
            if (page < 1)
                throw new ArgumentOutOfRangeException(
                    nameof(page), "Page must be greater than 0.");

            if (limit < 1 || limit > 100)
                throw new ArgumentOutOfRangeException(
                    nameof(limit), "Limit must be between 1 and 100.");

            var totalVoters = await (
                from v in _votingDbContext.voter.AsNoTracking()
                join a in _votingDbContext.auth.AsNoTracking()
                    on v.voter_id equals a.voter_id
                where a.role == "Voter"
                select v.voter_id
            ).CountAsync();

            var voters = await (
                from v in _votingDbContext.voter.AsNoTracking()
                join a in _votingDbContext.auth.AsNoTracking()
                    on v.voter_id equals a.voter_id
                where a.role == "Voter"
                orderby v.voter_id
                select new VoterDTO
                {
                    voter_id = v.voter_id,
                    firstname = v.firstname,
                    lastname = v.lastname,
                    province = v.province,
                    city = v.city,
                    street = v.street,
                    postal = v.postal,
                    allowed = a.allowed,
                    role = a.role,
                    created_at = a.created_at
                })
                .Skip((page - 1) * limit)
                .Take(limit)
                .ToListAsync();

            return new VoterListDTO
            {
                voterCount = totalVoters,
                voters = voters
            };
        }


        public async Task<VoterDTO> GetVoterByID(Guid voter_id)
        {
            var voter = await (
                from v in _votingDbContext.voter
                join a in _votingDbContext.auth
                    on v.voter_id equals a.voter_id
                where v.voter_id == voter_id
                      && a.role == "Voter"
                select new VoterDTO
                {
                    voter_id = v.voter_id,
                    firstname = v.firstname,
                    lastname = v.lastname,
                    province = v.province,
                    city = v.city,
                    street = v.street,
                    postal = v.postal,
                    allowed = a.allowed,
                    role = a.role,
                    created_at = a.created_at
                }
            ).FirstOrDefaultAsync();

            if (voter == null) throw new KeyNotFoundException("Voter not found.");

            return voter;
        }


        //allow and block them from the services
        public async Task<AuthStatusUpdateDTO> Update_voter_access(updateStatusDto request)
        {
            var user = await _votingDbContext.auth.FirstOrDefaultAsync(au => au.voter_id == request.ID);

            if (user == null) throw new KeyNotFoundException("User not found");

            user.allowed = request.status;

            await _votingDbContext.SaveChangesAsync();

            AuthStatusUpdateDTO authStatusUpdateDTO = new AuthStatusUpdateDTO
            {
                voter_id = user.voter_id,
                allowed = user.allowed,
                role = user.role
            };

            return authStatusUpdateDTO;
        }



    // Approve or update an election assignment only for users with the voter role.
    public async Task<ElectionVoterDto> AssignVoterToElection(AssignElectionVoterDTO request)
        {
            // Validate the requested status.
            if (!Enum.IsDefined(typeof(ElectionVoterStatus), request.status))throw new ArgumentException("Invalid election voter status.");
            

            // Ensure the user exists in auth and has the voter role.
            var isVoter = await _votingDbContext.auth
                .AnyAsync(a =>
                    a.voter_id == request.voter_id &&
                    a.role == "Voter");

            if (!isVoter) throw new UnauthorizedAccessException("User does not exist or does not have the voter role.");
            

            // Find the existing election-voter assignment.
            var assignment = await _votingDbContext.election_voter
                .FirstOrDefaultAsync(ev =>
                    ev.election_id == request.election_id &&
                    ev.voter_id == request.voter_id);

            if (assignment == null) throw new KeyNotFoundException("This voter is not assigned to this election.");
            

            // Update the assignment status.
            assignment.status = request.status;

            await _votingDbContext.SaveChangesAsync();

            // Return the updated election-voter information.
            return new ElectionVoterDto
            {
                election_id = assignment.election_id,
                voter_id = assignment.voter_id,
                voted = assignment.has_voted,
                status = assignment.status
            };
        }








        //manage admins
        public async Task<GetAdminDetailsDTO> NewAdmin(newAdminDTO request)
        {
            var vote_transaction = await _votingDbContext.Database.BeginTransactionAsync();

            try {
                var adminUser = await _votingDbContext.auth.FirstOrDefaultAsync(au => au.id_number == request.id_number);

                if (adminUser != null) throw new Exception("User already exist");

                //hash password
                var passwordHasher = new PasswordHasher<object>();
                string hashedPassword = passwordHasher.HashPassword(
                    null!,
                    request.password
                );

                //create auth entity
                Auth newAdmin = new Auth
                {
                    voter_id = Guid.NewGuid(),
                    id_number = request.id_number,
                    face_embedding = Array.Empty<double>(),
                    password = hashedPassword,
                    allowed = true,
                    role = "Admin",
                    created_at = DateTime.UtcNow
                };

                //create user entity
                Voter user = new Voter
                {
                    voter_id = newAdmin.voter_id,
                    firstname = request.firstname,
                    lastname = request.lastname,
                    province = request.province,
                    city = request.city,
                    street = string.Empty,//hide street location of admin users
                    postal = request.postal,
                };


                await _votingDbContext.auth.AddAsync(newAdmin);
                await _votingDbContext.voter.AddAsync(user);

                await _votingDbContext.SaveChangesAsync();

                await vote_transaction.CommitAsync();


                var newAddedUser = await (
                    from v in _votingDbContext.voter
                    join a in _votingDbContext.auth
                        on v.voter_id equals a.voter_id
                    where v.voter_id == user.voter_id
                    select new GetAdminDetailsDTO
                    {
                        user_id = v.voter_id,
                        allowed = a.allowed,
                        firstname = v.firstname,
                        lastname = v.lastname,
                        province = v.province,
                        city = v.city,
                        postal = v.postal,
                        role = a.role,
                        created_at = a.created_at
                    }
                ).FirstOrDefaultAsync();

                if (newAddedUser == null) throw new Exception("No user found");

                return newAddedUser;


            }
            catch (Exception ex) {
                await vote_transaction.RollbackAsync();
                throw new Exception(ex.Message);
            }
        }


        public async Task<GetAdminDetailsDTO> UpdateAdmin(Guid userId, UpdateAdmin request)
        {
            await using var transaction = await _votingDbContext.Database.BeginTransactionAsync();

            try
            {
                var auth = await _votingDbContext.auth .FirstOrDefaultAsync(a => a.voter_id == userId);

                if (auth == null) throw new KeyNotFoundException("User not found.");

                if (auth.role != "Admin") throw new InvalidOperationException("The specified user is not an admin.");

                var voter = await _votingDbContext.voter.FirstOrDefaultAsync(v => v.voter_id == userId);

                if (voter == null) throw new KeyNotFoundException("Admin profile not found.");

                // Update voter details
                voter.firstname = request.firstname;
                voter.lastname = request.lastname;
                voter.province = request.province;
                voter.city = request.city;
                voter.postal = request.postal;

                // Update authentication details
                auth.allowed = request.allowed;

                if (request.role != "Admin" && request.role != "Voter")
                    throw new ArgumentException("Invalid role.");

                auth.role = request.role;

                await _votingDbContext.SaveChangesAsync();

                var updatedAdmin = new GetAdminDetailsDTO
                {
                    user_id = voter.voter_id,
                    allowed = auth.allowed,
                    firstname = voter.firstname,
                    lastname = voter.lastname,
                    province = voter.province,
                    city = voter.city,
                    postal = voter.postal,
                    role = auth.role,
                    created_at = auth.created_at
                };

                await transaction.CommitAsync();

                return updatedAdmin;
            }
            catch
            {
                await transaction.RollbackAsync();
                throw;
            }
        }



        public async Task<List<GetAdminDetailsDTO>> GetAllAdmins()
        {
            var admins = await (
                from a in _votingDbContext.auth
                join v in _votingDbContext.voter
                    on a.voter_id equals v.voter_id
                where a.role == "Admin"
                select new GetAdminDetailsDTO
                {
                    user_id = a.voter_id,
                    firstname = v.firstname,
                    lastname = v.lastname,
                    role = a.role,
                    allowed = a.allowed,
                    created_at = a.created_at,
                    province = v.province,
                    city = v.city,
                    postal = v.postal
                }
            ).ToListAsync();

            return admins;
        }


    }
}
