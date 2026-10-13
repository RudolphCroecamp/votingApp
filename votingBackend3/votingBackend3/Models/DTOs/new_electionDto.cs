using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.DTOs
{
    public class new_electionDto
    {

        [NotNull, Required]
        public string election_name { get; set; } = string.Empty;
        [NotNull, Required]
        public string election_type { get; set; } = string.Empty;
        [NotNull, Required]
        public DateOnly start_date { get; set; }
        [NotNull, Required]
        public DateOnly end_date { get; set; }
    }
}
