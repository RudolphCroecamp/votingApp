
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using System.Text;
using votingBackend3.Models.Entities;

namespace votingBackend3.Data
{
    public static class DbSeederOld
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



        //seed

        public static async Task SeedExtendedAsync(VotingDbContext context)
        {
            var now = DateTime.UtcNow;

            // 1. Voting tokens
            // Development-only raw tokens for testing.
            // These are NOT associated with a voter in the database.
            var testTokens = new[]
            {
        new
        {
            Id = Guid.Parse("50000000-0000-0000-0000-000000000001"),
            RawToken = "DEV_TEST_VOTE_TOKEN_001"
        },
        new
        {
            Id = Guid.Parse("50000000-0000-0000-0000-000000000002"),
            RawToken = "DEV_TEST_VOTE_TOKEN_002"
        }
    };

            foreach (var item in testTokens)
            {
                if (!await context.voting_token.AnyAsync(
                    t => t.token_id == item.Id))
                {
                    var hash = Convert.ToBase64String(
                        SHA256.HashData(
                            Encoding.UTF8.GetBytes(item.RawToken)));

                    context.voting_token.Add(new VotingToken
                    {
                        token_id = item.Id,
                        TokenHash = hash,
                        election_id = ElectionId,
                        IssuedAt = now,
                        isUsed = false
                    });
                }
            }

            // 2. Sample election document metadata
            var documents = new[]
            {
        new DocumentInfo
        {
            fileId = Guid.Parse(
                "60000000-0000-0000-0000-000000000001"),
            voter_id = Voter1Id,
            FileName = "sample_voter_document_001.pdf",
            fileType = "application/pdf",
            uploadDate = now.AddDays(-5)
        },
        new DocumentInfo
        {
            fileId = Guid.Parse(
                "60000000-0000-0000-0000-000000000002"),
            voter_id = Voter2Id,
            FileName = "sample_voter_document_002.pdf",
            fileType = "application/pdf",
            uploadDate = now.AddDays(-2)
        }
    };

            foreach (var document in documents)
            {
                if (!await context.DocumentInfo.AnyAsync(
                    d => d.fileId == document.fileId))
                {
                    context.DocumentInfo.Add(document);
                }
            }

            // 3. Sample authentication sessions
            var sessions = new[]
            {
        new AuthSession
        {
            jti = Guid.Parse(
                "70000000-0000-0000-0000-000000000001"),
            voter_id = Voter1Id,
            created_at = now.AddMinutes(-10),
            expires_at = now.AddHours(2),
            revoked_at = null
        },
        new AuthSession
        {
            jti = Guid.Parse(
                "70000000-0000-0000-0000-000000000002"),
            voter_id = Voter2Id,
            created_at = now.AddDays(-2),
            expires_at = now.AddDays(-1),
            revoked_at = null
        },
        new AuthSession
        {
            jti = Guid.Parse(
                "70000000-0000-0000-0000-000000000003"),
            voter_id = Voter3Id,
            created_at = now.AddHours(-5),
            expires_at = now.AddHours(19),
            revoked_at = now.AddHours(-1)
        }
    };

            foreach (var session in sessions)
            {
                if (!await context.auth_sessions.AnyAsync(
                    s => s.jti == session.jti))
                {
                    context.auth_sessions.Add(session);
                }
            }

            await context.SaveChangesAsync();
        }









        //seed blockchain
        public static async Task SeedBlockchainAsync(BlocksDbContext context)
        {
            // Ensure the chain has a genesis block.
            if (!await context.blocks.AnyAsync())
            {
                var genesis = new Block(
                    0,
                    DateTime.UtcNow,
                    "genesis block",
                    "");

                context.blocks.Add(genesis);
                await context.SaveChangesAsync();
            }

            var testData = new[]
            {
        "SEED_TEST_BLOCK_1",
        "SEED_TEST_BLOCK_2"
    };

            foreach (var data in testData)
            {
                // Don't append a duplicate if this test block exists.
                if (await context.blocks.AnyAsync(b => b.Data == data))
                    continue;

                var latest = await context.blocks
                    .OrderByDescending(b => b.index)
                    .FirstAsync();

                var nextBlock = new Block(
                    latest.index + 1,
                    DateTime.UtcNow,
                    data,
                    latest.hash);

                context.blocks.Add(nextBlock);
                await context.SaveChangesAsync();
            }
        }

    }
}
