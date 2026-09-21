using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.Entities
{
    public class Party
    {
        [Key]
        public Guid party_id { get; set; } = Guid.NewGuid();
        [NotNull, Required]
        public string party_name { get; set; } = string.Empty;
        [NotNull, Required]
        public string party_description { get; set; } = string.Empty;
        [NotNull, Required]
        public bool status { get; set; } = false;//allowed in current election or not



        public ICollection<ElectionParty> ElectionParties { get; set; } = new List<ElectionParty>();
    }
}
