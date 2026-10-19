
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using System.Text;
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

            // 3. Seed real South African political parties.
            // Party IDs 1-3 reuse the original demo IDs; update those records if they already exist.
            // All party records are for development/demo purposes and do not imply election results.
            var parties = new[]
            {
                new Party { party_id = Party1Id, name = "African National Congress", abbreviation = "ANC", description = "South African political party.", allowed = true },
                new Party { party_id = Party2Id, name = "Democratic Alliance", abbreviation = "DA", description = "South African political party.", allowed = true },
                new Party { party_id = Party3Id, name = "Economic Freedom Fighters", abbreviation = "EFF", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000004"), name = "Inkatha Freedom Party", abbreviation = "IFP", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000005"), name = "uMkhonto weSizwe Party", abbreviation = "MK", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000006"), name = "Patriotic Alliance", abbreviation = "PA", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000007"), name = "Freedom Front Plus", abbreviation = "FF Plus", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000008"), name = "GOOD", abbreviation = "GOOD", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000009"), name = "ActionSA", abbreviation = "ActionSA", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000010"), name = "Build One South Africa", abbreviation = "BOSA", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000011"), name = "African Christian Democratic Party", abbreviation = "ACDP", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000012"), name = "African Transformation Movement", abbreviation = "ATM", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000013"), name = "Al Jama-ah", abbreviation = "ALJAMA-AH", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000014"), name = "United Democratic Movement", abbreviation = "UDM", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000015"), name = "Congress of the People", abbreviation = "COPE", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000016"), name = "African Independent Congress", abbreviation = "AIC", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000017"), name = "Pan Africanist Congress of Azania", abbreviation = "PAC", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000018"), name = "African People's Convention", abbreviation = "APC", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000019"), name = "National Freedom Party", abbreviation = "NFP", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000020"), name = "RISE Mzansi", abbreviation = "RISE", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000021"), name = "African Christian Alliance", abbreviation = "ACA", description = "South African political party.", allowed = true },
                new Party { party_id = Guid.Parse("40000000-0000-0000-0000-000000000022"), name = "National Coloured Congress", abbreviation = "NCC", description = "South African political party.", allowed = true }
            };

            foreach (var party in parties)
            {
                var existingParty = await context.party
                    .FirstOrDefaultAsync(p => p.party_id == party.party_id);

                if (existingParty == null)
                {
                    context.party.Add(party);
                }
                else
                {
                    // Update legacy placeholder party names on existing development databases.
                    existingParty.name = party.name;
                    existingParty.abbreviation = party.abbreviation;
                    existingParty.description = party.description;
                    existingParty.allowed = party.allowed;
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

            // 5. Link all 22 seeded parties to the demo election.
            var electionPartyLinks = parties.Select(party => new ElectionParty
            {
                election_id = ElectionId,
                party_id = party.party_id
            }).ToArray();

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

            // Add a larger, linked demo dataset for dashboard and election-results testing.
            await SeedRealisticResultsAsync(context);
        }

        private static async Task SeedRealisticResultsAsync(VotingDbContext context)
        {
            var now = DateTime.UtcNow;
            var today = DateOnly.FromDateTime(now);
            var hasher = new PasswordHasher<Auth>();

            // Additional stable IDs start at 4 so they do not overlap the original demo voters.
            var firstNames = new[]
            {
                "Ayanda", "Nomsa", "Kagiso", "Zanele", "Bongani", "Naledi", "Lwazi", "Amahle",
                "Themba", "Nandi", "Sibusiso", "Palesa", "Mandla", "Thandeka", "Karabo", "Lindiwe",
                "Musa", "Refilwe", "Nkosinathi", "Busisiwe", "Tshepo", "Zinhle", "Mpho", "Precious",
                "Vuyo", "Lerato", "Andile", "Dineo", "Siyabonga", "Nomvula", "Kabelo", "Portia",
                "Bhekizizwe", "Onthatile", "Jabu", "Yolanda", "Tumelo", "Khanyisile", "Sanele", "Boitumelo"
            };
            var surnames = new[]
            {
                "Mthembu", "Van der Merwe", "Molefe", "Pillay", "Ndlovu", "Jacobs", "Khumalo", "Botha",
                "Mahlangu", "Naidoo", "Cele", "Maseko", "Sithole", "Adams", "Mokoena", "Mabena",
                "Dube", "Williams", "Zulu", "Molefe", "Mkhize", "Pretorius", "Nkosi", "Mahlangu",
                "Gumede", "Mokoena", "Mthethwa", "Daniels", "Radebe", "Mokoena", "Modise", "Petersen",
                "Shabalala", "Molefe", "Ngcobo", "Morris", "Molefe", "Mkhwanazi", "Zwane", "Mokoena"
            };

            var locations = new (string Province, string City, int Postal, string Street)[]
            {
                ("Western Cape", "Cape Town", 8001, "Long Street"),
                ("Gauteng", "Pretoria", 0002, "Church Street"),
                ("KwaZulu-Natal", "Pietermaritzburg", 3201, "Chief Albert Luthuli Street"),
                ("Eastern Cape", "East London", 5201, "Oxford Street"),
                ("Free State", "Bloemfontein", 9301, "Nelson Mandela Drive"),
                ("Limpopo", "Polokwane", 0699, "Landdros Mare Street"),
                ("Mpumalanga", "Mbombela", 1200, "Samora Machel Drive"),
                ("North West", "Mahikeng", 2745, "Nelson Mandela Drive"),
                ("Northern Cape", "Kimberley", 8301, "Du Toitspan Road"),
                ("Gauteng", "Johannesburg", 2001, "Commissioner Street")
            };

            // 1. Add 40 fictional voters and their matching authentication records.
            for (var i = 4; i <= 43; i++)
            {
                var voterId = Guid.Parse($"20000000-0000-0000-0000-{i:D12}");
                var location = locations[(i - 4) % locations.Length];

                if (!await context.voter.AnyAsync(v => v.voter_id == voterId))
                {
                    context.voter.Add(new Voter
                    {
                        voter_id = voterId,
                        firstname = firstNames[i - 4],
                        lastname = surnames[i - 4],
                        province = location.Province,
                        city = location.City,
                        street = $"{(i * 7) % 180 + 1} {location.Street}",
                        postal = location.Postal
                    });
                }

                if (!await context.auth.AnyAsync(a => a.voter_id == voterId))
                {
                    var account = new Auth
                    {
                        voter_id = voterId,
                        id_number = $"900101500{i:D4}",
                        password = string.Empty,
                        face_embedding = Array.Empty<double>(),
                        allowed = i % 13 != 0, // A few accounts are disabled for admin testing.
                        role = "Voter",
                        created_at = now.AddDays(-((i * 11) % 420))
                    };
                    account.password = hasher.HashPassword(account, "Voter123!ChangeMe");
                    context.auth.Add(account);
                }
            }
            await context.SaveChangesAsync();

            // 2. Use the 22 real South African parties seeded by SeedAsync.
            // Do not create fictional parties here: the same IDs are used by the main party seeder.
            var allPartyIds = Enumerable.Range(1, 22)
                .Select(i => Guid.Parse($"40000000-0000-0000-0000-{i:D12}"))
                .ToArray();

            // 3. Past and upcoming elections complement the existing active sample election.
            var pastElectionId = Guid.Parse("30000000-0000-0000-0000-000000000002");
            var upcomingElectionId = Guid.Parse("30000000-0000-0000-0000-000000000003");
            var elections = new[]
            {
                new Election
                {
                    election_id = pastElectionId,
                    election_name = "Community Council Election 2025 (Demo)",
                    election_type = "Local Government",
                    start_date = today.AddDays(-150),
                    end_date = today.AddDays(-147),
                    isActive = false
                },
                new Election
                {
                    election_id = upcomingElectionId,
                    election_name = "Youth Representative Election (Demo)",
                    election_type = "Representative",
                    start_date = today.AddDays(45),
                    end_date = today.AddDays(48),
                    isActive = false
                }
            };
            foreach (var election in elections)
            {
                if (!await context.election.AnyAsync(e => e.election_id == election.election_id))
                    context.election.Add(election);
            }
            await context.SaveChangesAsync();

            var electionIds = new[] { ElectionId, pastElectionId, upcomingElectionId };
            foreach (var electionId in electionIds)
            {
                foreach (var partyId in allPartyIds)
                {
                    if (!await context.election_party.AnyAsync(ep => ep.election_id == electionId && ep.party_id == partyId))
                        context.election_party.Add(new ElectionParty { election_id = electionId, party_id = partyId });
                }
            }
            await context.SaveChangesAsync();

            // 4. Register voters with a realistic mix of approval states for each election.
            foreach (var i in Enumerable.Range(4, 40))
            {
                var voterId = Guid.Parse($"20000000-0000-0000-0000-{i:D12}");
                foreach (var electionId in electionIds)
                {
                    // The upcoming election has more pending applications; past election has mostly decisions completed.
                    var status = electionId == upcomingElectionId && i % 3 == 0
                        ? ElectionVoterStatus.Pending
                        : i % 11 == 0
                            ? ElectionVoterStatus.Rejected
                            : i % 7 == 0
                                ? ElectionVoterStatus.Pending
                                : ElectionVoterStatus.Approved;

                    if (!await context.election_voter.AnyAsync(ev => ev.election_id == electionId && ev.voter_id == voterId))
                    {
                        context.election_voter.Add(new ElectionVoter
                        {
                            election_id = electionId,
                            voter_id = voterId,
                            status = status,
                            has_voted = false
                        });
                    }
                }
            }
            await context.SaveChangesAsync();

            // 5. Seed valid vote/token pairs for approved voters only. Party selection is deterministic,
            // producing varied but repeatable totals without pretending these are real election results.
            var votePlans = new List<(Guid ElectionId, int VoterNumber, int PartyIndex)>();
            for (var i = 4; i <= 43; i++)
            {
                // Past election: most approved voters cast a vote.
                if (i % 11 != 0 && i % 7 != 0 && i % 5 != 0)
                    votePlans.Add((pastElectionId, i, (i * 3 + i / 2) % allPartyIds.Length));

                // Existing election is active: only a subset have voted so far.
                if (i <= 28 && i % 11 != 0 && i % 7 != 0 && i % 3 != 0)
                    votePlans.Add((ElectionId, i, (i * 5 + 1) % allPartyIds.Length));
            }

            foreach (var plan in votePlans)
            {
                var voterId = Guid.Parse($"20000000-0000-0000-0000-{plan.VoterNumber:D12}");
                var tokenNumber = (plan.ElectionId == pastElectionId ? 100000 : 200000) + plan.VoterNumber;
                var tokenId = Guid.Parse($"50000000-0000-0000-0000-{tokenNumber:D12}");
                var voteId = Guid.Parse($"80000000-0000-0000-0000-{tokenNumber:D12}");
                var rawToken = $"DEMO_ONLY_{plan.ElectionId:N}_{plan.VoterNumber:D4}";
                var tokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(rawToken)));
                var election = await context.election.FirstAsync(e => e.election_id == plan.ElectionId);
                var start = plan.ElectionId == pastElectionId
                    ? DateTime.SpecifyKind(
                        election.start_date.ToDateTime(TimeOnly.FromTimeSpan(TimeSpan.FromHours(9))),
                        DateTimeKind.Utc)
                    : now.AddMinutes(-Math.Max(5, plan.VoterNumber * 17));
                var createdAt = start.AddMinutes(plan.VoterNumber * 13);

                if (!await context.voting_token.AnyAsync(t => t.token_id == tokenId))
                {
                    context.voting_token.Add(new VotingToken
                    {
                        token_id = tokenId,
                        TokenHash = tokenHash,
                        election_id = plan.ElectionId,
                        IssuedAt = createdAt,
                        isUsed = true
                    });
                }

                if (!await context.vote.AnyAsync(v => v.vote_id == voteId))
                {
                    context.vote.Add(new Vote
                    {
                        vote_id = voteId,
                        token_id = tokenId,
                        election_id = plan.ElectionId,
                        party_id = allPartyIds[plan.PartyIndex],
                        created_at = createdAt.AddMinutes(1)
                    });
                }

                var registration = await context.election_voter.FirstOrDefaultAsync(
                    ev => ev.election_id == plan.ElectionId && ev.voter_id == voterId);
                if (registration != null && registration.status == ElectionVoterStatus.Approved)
                    registration.has_voted = true;
            }

            // A few unused tokens let the UI/API exercise the issued-but-not-cast state.
            for (var i = 30; i <= 34; i++)
            {
                var tokenId = Guid.Parse($"50000000-0000-0000-0000-{300000 + i:D12}");
                if (!await context.voting_token.AnyAsync(t => t.token_id == tokenId))
                {
                    var rawToken = $"DEMO_UNUSED_TOKEN_{i:D4}";
                    context.voting_token.Add(new VotingToken
                    {
                        token_id = tokenId,
                        TokenHash = Convert.ToBase64String(SHA256.HashData(Encoding.UTF8.GetBytes(rawToken))),
                        election_id = ElectionId,
                        IssuedAt = now.AddMinutes(-i * 9),
                        isUsed = false
                    });
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
