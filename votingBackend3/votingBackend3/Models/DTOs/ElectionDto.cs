using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class ElectionDto
    {
        [Required]
        public Guid election_id { get; set; } = Guid.NewGuid();
        [Required, MaxLength(150)]
        public string election_name { get; set; } = string.Empty;
        [Required]
        public string election_type { get; set; } = string.Empty;
        [Required]
        public DateOnly start_date { get; set; }
        [Required]
        public DateOnly end_date { get; set; }
        [Required]
        public bool isActive { get; set; } = false;
    }
}
