using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class Party
    {
        [Key]
        public Guid party_id { get; set; } = Guid.NewGuid();

        [Required, MaxLength(150)]
        public string name { get; set; } = string.Empty;

        [MaxLength(20)]
        public string abbreviation { get; set; }

        [Required, MaxLength(500)]
        public string description { get; set; } = string.Empty;

        public bool allowed { get; set; } = true;

        public DateTime created_at { get; set; } = DateTime.UtcNow;

        public ICollection<ElectionParty> ElectionParties { get; set; }= new List<ElectionParty>();
    }
}