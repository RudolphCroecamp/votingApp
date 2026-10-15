using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class DocumentInfo
    {
        [Key]
        public Guid fileId { get; set; } = Guid.Empty;
        [Required]
        public string FileName { get; set; } = string.Empty;
        [Required]
        public string fileType { get; set; } = string.Empty;
        public DateTime uploadDate { get; set; } = DateTime.UtcNow;

        // Foreign key
        [Required]
        public Guid voter_id { get; set; }
        public Voter Voter { get; set; }
    }
}
