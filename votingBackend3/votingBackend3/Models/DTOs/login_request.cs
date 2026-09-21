namespace votingBackend3.Models.DTOs
{
    public class login_request
    {
        public string id_number { get; set; } = string.Empty;

        public double[] face_embedding { get; set; } = Array.Empty<double>();
    }
}
