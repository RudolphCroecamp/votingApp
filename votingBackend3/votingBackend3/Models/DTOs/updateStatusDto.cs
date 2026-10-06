using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.DTOs
{
    public class updateStatusDto
    {
        [Required]
        public Guid ID { get; set; }
        [Required]
        public bool status { get; set; }
    }
}
