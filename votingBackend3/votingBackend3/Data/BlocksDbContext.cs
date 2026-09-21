using Microsoft.EntityFrameworkCore;
using votingBackend3.Models.Entities;

namespace votingBackend3.Data
{
    public class BlocksDbContext : DbContext
    {

        public BlocksDbContext(DbContextOptions<BlocksDbContext> options)
            : base(options)
        {
        }

        public DbSet<Block> blocks { get; set; }



        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Block>()
                .HasKey(b => b.index);
        }


    }
}
