
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using votingBackend3.Models.Entities;

namespace votingBackend3.Data
{
    public static class DbSeeder
    {
        // Stable IDs make the seeder safe to run repeatedly.
        private static readonly Guid AdminId =
            Guid.Parse("10000000-0000-0000-0000-000000000001");

        private static readonly Guid Voter1Id =
            Guid.Parse("20000000-0000-0000-0000-000000000001");

        private static readonly Guid Voter2Id =
            Guid.Parse("20000000-0000-0000-0000-000000000002");

        private static readonly Guid Voter3Id =
            Guid.Parse("20000000-0000-0000-0000-000000000003");

        private static readonly Guid ElectionId =
            Guid.Parse("30000000-0000-0000-0000-000000000001");

        private static readonly Guid Party1Id =
            Guid.Parse("40000000-0000-0000-0000-000000000001");

        private static readonly Guid Party2Id =
            Guid.Parse("40000000-0000-0000-0000-000000000002");

        private static readonly Guid Party3Id =
            Guid.Parse("40000000-0000-0000-0000-000000000003");

        public static async Task SeedAsync(VotingDbContext context)
        {
            // Do not create sample records if their IDs already exist.
            var hasher = new PasswordHasher<Auth>();

            // 1. Create voters
            var voters = new[]
            {
                new Voter
                {
                    voter_id = AdminId,
                    firstname = "System",
                    lastname = "Administrator",
                    province = "Western Cape",
                    city = "Cape Town",
                    street = "Admin Test Street",
                    postal = 8001
                },
                new Voter
                {
                    voter_id = Voter1Id,
                    firstname = "Thabo",
                    lastname = "Mokoena",
                    province = "Gauteng",
                    city = "Johannesburg",
                    street = "Sample Street 1",
                    postal = 2001
                },
                new Voter
                {
                    voter_id = Voter2Id,
                    firstname = "Lerato",
                    lastname = "Nkosi",
                    province = "KwaZulu-Natal",
                    city = "Durban",
                    street = "Sample Street 2",
                    postal = 4001
                },
                new Voter
                {
                    voter_id = Voter3Id,
                    firstname = "Sipho",
                    lastname = "Dlamini",
                    province = "Eastern Cape",
                    city = "Gqeberha",
                    street = "Sample Street 3",
                    postal = 6001
                }
            };

            foreach (var voter in voters)
            {
                if (!await context.voter.AnyAsync(
                    v => v.voter_id == voter.voter_id))
                {
                    context.voter.Add(voter);
                }
            }

            await context.SaveChangesAsync();

            // 2. Create authentication accounts
            var accounts = new[]
            {
                new Auth
                {
                    voter_id = AdminId,
                    id_number = "0000000000001",
                    password = "",
                    face_embedding = Array.Empty<double>(),
                    allowed = true,
                    role = "Admin",
                    created_at = DateTime.UtcNow
                },
                new Auth
                {
                    voter_id = Voter1Id,
                    id_number = "9001015009087",
                    password = "",
                    face_embedding = Array.Empty<double>(),
                    allowed = true,
                    role = "Voter",
                    created_at = DateTime.UtcNow
                },
                new Auth
                {
                    voter_id = Voter2Id,
                    id_number = "9202025009088",
                    password = "",
                    face_embedding = Array.Empty<double>(),
                    allowed = true,
                    role = "Voter",
                    created_at = DateTime.UtcNow
                },
                new Auth
                {
                    voter_id = Voter3Id,
                    id_number = "9503035009089",
                    password = "",
                    face_embedding = Array.Empty<double>(),
                    allowed = true,
                    role = "Voter",
                    created_at = DateTime.UtcNow
                }
            };

            var passwords = new[]
            {
                "Admin123!ChangeMe",
                "Voter123!ChangeMe",
                "Voter123!ChangeMe",
                "Voter123!ChangeMe"
            };

            for (int i = 0; i < accounts.Length; i++)
            {
                var account = accounts[i];

                if (!await context.auth.AnyAsync(
                    a => a.voter_id == account.voter_id))
                {
                    account.password = hasher.HashPassword(
                        account, passwords[i]);

                    context.auth.Add(account);
                }
            }

            await context.SaveChangesAsync();

            // 3. Create political parties
            var parties = new[]
            {
                new Party
                {
                    party_id = Party1Id,
                    name = "Ubuntu Development Party",
                    abbreviation = "UDP",
                    description =
                        "Sample party focused on public services and development.",
                    allowed = true
                },
                new Party
                {
                    party_id = Party2Id,
                    name = "National Progress Alliance",
                    abbreviation = "NPA",
                    description =
                        "Sample party focused on economic growth and employment.",
                    allowed = true
                },
                new Party
                {
                    party_id = Party3Id,
                    name = "Community First Movement",
                    abbreviation = "CFM",
                    description =
                        "Sample party focused on community-led governance.",
                    allowed = true
                }
            };

            foreach (var party in parties)
            {
                if (!await context.party.AnyAsync(
                    p => p.party_id == party.party_id))
                {
                    context.party.Add(party);
                }
            }

            await context.SaveChangesAsync();

            // 4. Create a sample election
            if (!await context.election.AnyAsync(
                e => e.election_id == ElectionId))
            {
                var today = DateOnly.FromDateTime(DateTime.UtcNow);

                context.election.Add(new Election
                {
                    election_id = ElectionId,
                    election_name = "Sample General Election",
                    election_type = "General",
                    start_date = today,
                    end_date = today.AddDays(30),
                    isActive = true
                });

                await context.SaveChangesAsync();
            }

            // 5. Link parties to the election
            var electionPartyLinks = new[]
            {
                new ElectionParty
                {
                    election_id = ElectionId,
                    party_id = Party1Id
                },
                new ElectionParty
                {
                    election_id = ElectionId,
                    party_id = Party2Id
                },
                new ElectionParty
                {
                    election_id = ElectionId,
                    party_id = Party3Id
                }
            };

            foreach (var link in electionPartyLinks)
            {
                if (!await context.election_party.AnyAsync(
                    ep => ep.election_id == link.election_id
                       && ep.party_id == link.party_id))
                {
                    context.election_party.Add(link);
                }
            }

            await context.SaveChangesAsync();

            // 6. Register voters for the election
            var registrations = new[]
            {
                new ElectionVoter
                {
                    election_id = ElectionId,
                    voter_id = Voter1Id,
                    status = ElectionVoterStatus.Approved,
                    has_voted = false
                },
                new ElectionVoter
                {
                    election_id = ElectionId,
                    voter_id = Voter2Id,
                    status = ElectionVoterStatus.Pending,
                    has_voted = false
                },
                new ElectionVoter
                {
                    election_id = ElectionId,
                    voter_id = Voter3Id,
                    status = ElectionVoterStatus.Rejected,
                    has_voted = false
                }
            };

            foreach (var registration in registrations)
            {
                if (!await context.election_voter.AnyAsync(
                    ev => ev.election_id == registration.election_id
                       && ev.voter_id == registration.voter_id))
                {
                    context.election_voter.Add(registration);
                }
            }

            await context.SaveChangesAsync();
        }
    }
}
