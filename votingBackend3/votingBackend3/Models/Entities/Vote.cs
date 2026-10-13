using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class Vote
    {
        [Key, Required]
        public Guid vote_id { get; set; } = Guid.NewGuid();
        [Required]
        public Guid token_id { get; set; }//link to VotingToken -> used to vote
        [Required]
        public Guid election_id { get; set; }
        [Required]
        public Guid party_id { get; set; }
        [Required]
        public DateTime created_at { get; set; } = DateTime.UtcNow;
        //foreign keys
        public VotingToken votingToken { get; set; } = null!;
        public Election Election { get; set; } = null!;
        public Party Party { get; set; } = null!;

    }
}

