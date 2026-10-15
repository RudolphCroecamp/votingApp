using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class login_requestDto
    {
        [Required, MinLength(13), MaxLength(13)]
        public string id_number { get; set; } = string.Empty;
        [Required]
        public double[] face_embedding { get; set; } = Array.Empty<double>();
    }
}
