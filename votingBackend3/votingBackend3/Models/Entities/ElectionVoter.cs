using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public enum ElectionVoterStatus
    {
        Pending = 0,
        Approved = 1,
        Rejected = 2
    }

    public class ElectionVoter
    {
        [Required]
        public Guid election_id { get; set; }
        [Required]
        public Guid voter_id { get; set; }
        [Required]
        public ElectionVoterStatus status { get; set; } = ElectionVoterStatus.Pending;
        [Required]
        public bool has_voted { get; set; } = false;

        public Election Election { get; set; } = null!;
        public Voter Voter { get; set; } = null!;
    }
}

