using Microsoft.EntityFrameworkCore;
using votingBackend3.Models.Entities;

namespace votingBackend3.Data
{
    public class VotingDbContext : DbContext
    {
        public VotingDbContext(DbContextOptions<VotingDbContext> options)
            : base(options)
        {
        }

        public DbSet<Auth> auth { get; set; }
        public DbSet<Election> election { get; set; }
        public DbSet<ElectionParty> election_party { get; set; }
        public DbSet<ElectionVoter> election_voter { get; set; }
        public DbSet<Party> party { get; set; }
        public DbSet<Vote> vote { get; set; }
        public DbSet<Voter> voter { get; set; }
        public DbSet<VotingToken> voting_token { get; set; }
        public DbSet<DocumentInfo> DocumentInfo { get; set; }


        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            // ElectionVoter
            modelBuilder.Entity<ElectionVoter>()
                .HasKey(ev => new
                {
                    ev.election_id,
                    ev.voter_id
                });

            modelBuilder.Entity<ElectionVoter>()
                .HasOne(ev => ev.Election)
                .WithMany(e => e.ElectionVoters)
                .HasForeignKey(ev => ev.election_id);

            modelBuilder.Entity<ElectionVoter>()
                .HasOne(ev => ev.Voter)
                .WithMany(v => v.ElectionVoters)
                .HasForeignKey(ev => ev.voter_id);


            // ElectionParty
            modelBuilder.Entity<ElectionParty>()
                .HasKey(ep => new
                {
                    ep.election_id,
                    ep.party_id
                });

            modelBuilder.Entity<ElectionParty>()
                .HasOne(ep => ep.Election)
                .WithMany(e => e.ElectionParties)
                .HasForeignKey(ep => ep.election_id);

            modelBuilder.Entity<ElectionParty>()
                .HasOne(ep => ep.Party)
                .WithMany(p => p.ElectionParties)
                .HasForeignKey(ep => ep.party_id);


            // VotingToken -> Election
            modelBuilder.Entity<VotingToken>()
                .HasOne(vt => vt.Election)
                .WithMany(e => e.VotingTokens)
                .HasForeignKey(vt => vt.election_id);


            // VotingToken -> Vote
            modelBuilder.Entity<VotingToken>()
                .HasOne(vt => vt.Vote)
                .WithOne(v => v.votingToken)
                .HasForeignKey<Vote>(v => v.token_id);


            // Vote -> Election
            modelBuilder.Entity<Vote>()
                .HasOne(v => v.Election)
                .WithMany()
                .HasForeignKey(v => v.election_id);


            // Vote -> Party
            modelBuilder.Entity<Vote>()
                .HasOne(v => v.Party)
                .WithMany()
                .HasForeignKey(v => v.party_id);

            //documentInfo
            modelBuilder.Entity<DocumentInfo>()
                .HasKey(d => d.fileId);

            //documentInfo -> Voter
            modelBuilder.Entity<DocumentInfo>()
                .HasOne(d => d.Voter)
                .WithMany(v => v.Documents)
                .HasForeignKey(d => d.voter_id);

        }
    }
}