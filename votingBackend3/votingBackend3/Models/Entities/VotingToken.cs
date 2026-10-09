using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class VotingToken
    {
        [Key, Required]
        public Guid token_id { get; set; } = Guid.NewGuid();
        [Required, MaxLength(64)]//SHA256 hash is 32 bytes, which is 64 characters in hex
        public string TokenHash { get; set; } = string.Empty;
        [Required]
        public Guid election_id { get; set; } = Guid.Empty;
        [Required]
        public bool isUsed { get; set; } = false;
        [Required]
        public DateTime IssuedAt { get; set; } = DateTime.UtcNow;

        //foreign keys
        public Election Election { get; set; } = null!;
        public Vote? Vote { get; set; }
    }
}