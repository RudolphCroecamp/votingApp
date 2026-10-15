using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.DTOs
{
    public class new_electionDto
    {

        [Required]
        public string election_name { get; set; } = string.Empty;
        [Required]
        public string election_type { get; set; } = string.Empty;
        [Required]
        public DateOnly start_date { get; set; }
        [Required]
        public DateOnly end_date { get; set; }
    }
}
