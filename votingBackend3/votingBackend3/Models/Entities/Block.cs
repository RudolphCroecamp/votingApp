using System.ComponentModel.DataAnnotations;
using votingBackend3.Models.DTOs;

namespace votingBackend3.Models.Entities
{
    public class Block
    {
        [Key, Required]
        public int index { get; set; }
        [Required]
        public DateTime timestamp { get; set; }
        [Required]
        public string hash { get; set; } = string.Empty;
        [Required]
        public string Data { get; set; } = string.Empty;
        [Required]
        public string previousHash { get; set; } = string.Empty;

        public Block(int index, DateTime timestamp, string data, string previousHash)
        {
            this.index = index;
            this.timestamp = timestamp;
            this.Data = data;
            this.previousHash = previousHash;
            this.hash = CalculateHash();
        }

        public string CalculateHash()
        {
            using (var sha256 = System.Security.Cryptography.SHA256.Create())
            {
                var rawData = $"{index}-{timestamp.ToString()}-{Data}-{previousHash}";
                var bytes = System.Text.Encoding.UTF8.GetBytes(rawData);
                var hashBytes = sha256.ComputeHash(bytes);
                //return BitConverter.ToString(hashBytes).Replace("-", "").ToLower();
                return Convert.ToBase64String(hashBytes);
            }
        }
    }
}
