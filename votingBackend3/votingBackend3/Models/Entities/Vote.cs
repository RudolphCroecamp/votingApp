using System.ComponentModel.DataAnnotations;
using votingBackend3.Models.DTOs;

namespace votingBackend3.Models.Entities
{
    public class Vote
    {
        [Key]
        public Guid vote_id { get; set; } = Guid.NewGuid();
        public Guid token_id { get; set; }
        public VotingToken votingToken { get; set; } = null!;
        public Guid election_id { get; set; }
        public Election Election { get; set; } = null!;
        public Guid party_id { get; set; }
        public Party Party { get; set; } = null!;
        public string election_type { get; set; } = string.Empty;
        public DateTime created_at { get; set; } = DateTime.UtcNow;
    }
}

