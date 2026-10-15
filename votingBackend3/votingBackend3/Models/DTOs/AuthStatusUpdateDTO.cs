using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class AuthStatusUpdateDTO
    {
        [Required]
        public Guid voter_id { get; set; } = Guid.Empty;
        [Required]
        public bool allowed { get; set; } = false;
        [Required]
        public string role { get; set; } = string.Empty;
    }
}
