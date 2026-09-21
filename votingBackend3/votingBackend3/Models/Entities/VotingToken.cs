using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class VotingToken
    {
        [Key]
        public Guid token_id { get; set; } = Guid.NewGuid();
        [Required, MaxLength(64)]
        public string TokenHash { get; set; } = string.Empty;
        public Guid election_id { get; set; }
        public Election Election { get; set; } = null!;
        public bool isUsed { get; set; } = false;
        public DateTime IssuedAt { get; set; } = DateTime.UtcNow;
        public Vote? Vote { get; set; }
    }
}