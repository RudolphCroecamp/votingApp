using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.DTOs
{
    public class new_partyDto
    {
        [Required, MaxLength(150)]
        public string name { get; set; } = string.Empty;

        [Required, MaxLength(20)]
        public string abbreviation { get; set; } = string.Empty;

        [Required, MaxLength(500)]
        public string description { get; set; } = string.Empty;

        public bool allowed { get; set; } = true;
    }
}
