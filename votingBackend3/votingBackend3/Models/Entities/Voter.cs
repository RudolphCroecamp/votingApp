using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.Entities
{
    public class Voter
    {
        [Key]
        public Guid voter_id { get; set; } = Guid.NewGuid();
        [NotNull]
        public string firstname { get; set; } = string.Empty;
        [NotNull]
        public string lastname { get; set; } = string.Empty;
        [NotNull]
        public string province { get; set; } = string.Empty;
        [NotNull]
        public string city { get; set; } = string.Empty;
        public string street { get; set; } = string.Empty;
        [NotNull, DataType(DataType.PostalCode)]
        public int postal { get; set; }



        public ICollection<ElectionVoter> ElectionVoters { get; set; } = new List<ElectionVoter>();
    }
}
