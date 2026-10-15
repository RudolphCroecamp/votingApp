namespace votingBackend3.Models.DTOs
{
    public class DocumentResponse
    {
        public Guid fileId { get; set; }
        public string FileName { get; set; } = string.Empty;
        public string fileType { get; set; } = string.Empty;
    }
}