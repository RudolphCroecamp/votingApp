using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class ElectionParty
    {
        [Required]
        public Guid election_id { get; set; }
        [Required]
        public Guid party_id { get; set; }

        public Election Election { get; set; } = null!;
        public Party Party { get; set; } = null!;
    }
}
