using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace votingBackend3.Models.DTOs
{
    public class new_party
    {
        [NotNull, Required]
        public string party_name { get; set; } = string.Empty;
        [NotNull, Required]
        public string party_description { get; set; } = string.Empty;
    }
}
