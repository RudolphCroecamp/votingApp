using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.Entities
{
    public class Voter
    {
        [Key]
        public Guid voter_id { get; set; } = Guid.Empty;
        [Required]
        public string firstname { get; set; } = string.Empty;
        [Required]
        public string lastname { get; set; } = string.Empty;
        [Required]
        public string province { get; set; } = string.Empty;
        [Required]
        public string city { get; set; } = string.Empty;
        public string street { get; set; } = string.Empty;
        [Required, DataType(DataType.PostalCode)]
        public int postal { get; set; } = 0;



        public ICollection<ElectionVoter> ElectionVoters { get; set; } = new List<ElectionVoter>();
    }
}
