using System.ComponentModel.DataAnnotations;

namespace votingBackend3.Models.Entities
{
    public class DocumentInfo
    {
        [Key]
        public Guid fileId { get; set; } = Guid.Empty;
        public string FileName { get; set; } = string.Empty;
        public string fileType { get; set; } = string.Empty;
        public DateTime uploadDate { get; set; } = DateTime.UtcNow;

        // Foreign key
        public Guid voter_id { get; set; }

        public Voter Voter { get; set; }
    }
}
