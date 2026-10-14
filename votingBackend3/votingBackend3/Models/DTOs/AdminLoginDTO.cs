using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class AdminLoginDTO
    {
        [Required]
        public string id_number { get; set; } = string.Empty;
        [Required]
        public string password { get; set; } = string.Empty;
    }
}
