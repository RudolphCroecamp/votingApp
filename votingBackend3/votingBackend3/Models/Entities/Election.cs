using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.Entities
{
    public class Election
    {
        [Key]
        public Guid election_id { get; set; } = Guid.NewGuid();
        [NotNull, Required]
        public string election_name { get; set; } = string.Empty;
        [NotNull, Required]
        public string election_type { get; set; } = string.Empty;
        [NotNull, Required]
        public DateOnly start_date { get; set; }
        [NotNull, Required]
        public DateOnly end_date { get; set; }
        [NotNull, Required]
        public bool isActive { get; set; } = false;



        public ICollection<ElectionVoter> ElectionVoters { get; set; } = new List<ElectionVoter>();

        public ICollection<ElectionParty> ElectionParties { get; set; } = new List<ElectionParty>();

        public ICollection<VotingToken> VotingTokens { get; set; } = new List<VotingToken>();

    }
}
