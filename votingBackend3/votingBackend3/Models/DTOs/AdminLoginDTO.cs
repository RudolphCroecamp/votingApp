using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class AdminLoginDTO
    {
        [Required, MinLength(13), MaxLength(13)]
        public string id_number { get; set; } = string.Empty;
        [Required, MinLength(8), MaxLength(32)]
        public string password { get; set; } = string.Empty;
    }
}
