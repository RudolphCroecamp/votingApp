using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class logout_request
    {
        [Required]
        public string token { get; set; } = string.Empty;
    }
}