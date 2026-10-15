using Microsoft.AspNetCore.Http;
using Microsoft.EntityFrameworkCore;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;

namespace votingBackend3.Services
{
    public class DocumentService
    {
        private readonly VotingDbContext _votingDbContext;
        private readonly string _storagePath;

        private const long MaxFileSize = 5 * 1024 * 1024; // 5 MB per image

        private static readonly HashSet<string> AllowedExtensions =
            new(StringComparer.OrdinalIgnoreCase)
            {
                ".jpg",
                ".jpeg",
                ".png"
            };

        public DocumentService(
            VotingDbContext votingDbContext,
            IConfiguration configuration)
        {
            _votingDbContext = votingDbContext;

            _storagePath =
                configuration["DocumentStorage:Path"]
                ?? Path.Combine(
                    Directory.GetCurrentDirectory(),
                    "VotingDocuments");
        }

        // Expects two files named id_front and id_back.
        public async Task<List<DocumentInfo>> UploadFiles(
            Guid voterId,
            List<IFormFile> files)
        {
            if (voterId == Guid.Empty)
                throw new ArgumentException("Invalid voter ID.");

            if (files == null || files.Count != 2) throw new ArgumentException("Exactly two ID images are required.");

            var front = files.FirstOrDefault(f =>
                string.Equals(
                    f.Name, "id_front",
                    StringComparison.OrdinalIgnoreCase));

            var back = files.FirstOrDefault(f =>
                string.Equals(
                    f.Name, "id_back",
                    StringComparison.OrdinalIgnoreCase));

            if (front == null || back == null) throw new ArgumentException("Both id_front and id_back are required.");

            ValidateFile(front);
            ValidateFile(back);

            Directory.CreateDirectory(_storagePath);

            var savedPaths = new List<string>();
            var documents = new List<DocumentInfo>();

            try
            {
                documents.Add(await SaveFile(front, voterId, "ID_Front", savedPaths));

                documents.Add(await SaveFile(back, voterId, "ID_Back", savedPaths));

                await _votingDbContext.DocumentInfo.AddRangeAsync(documents);
                await _votingDbContext.SaveChangesAsync();

                return documents;
            }
            catch
            {
                // Avoid leaving files behind if saving fails.
                foreach (var path in savedPaths)
                {
                    if (File.Exists(path))
                        File.Delete(path);
                }

                throw;
            }
        }

        public async Task<List<DocumentResponse>> GetFiles(Guid voterId)
        {
            return await _votingDbContext.DocumentInfo
                .Where(d => d.voter_id == voterId)
                .Select(d => new DocumentResponse
                {
                    fileId = d.fileId,
                    FileName = d.FileName,
                    fileType = d.fileType
                })
                .ToListAsync();
        }

        // Returns the image data for a specific voter-owned document.
        public async Task<DocumentFileResult?> GetFile(
            Guid voterId,
            Guid fileId)
        {
            var document = await _votingDbContext.DocumentInfo
                .AsNoTracking()
                .FirstOrDefaultAsync(d =>
                    d.fileId == fileId &&
                    d.voter_id == voterId);

            if (document == null) return null;

            var path = Path.Combine(
                _storagePath,
                document.fileId.ToString() +
                document.fileType);

            if (!File.Exists(path)) return null;

            var bytes = await File.ReadAllBytesAsync(path);

            var contentType = Path.GetExtension(path)
                .ToLowerInvariant() switch
            {
                ".jpg" or ".jpeg" => "image/jpeg",
                ".png" => "image/png",
                _ => "application/octet-stream"
            };

            return new DocumentFileResult(
                document.fileId,
                document.FileName,
                contentType,
                bytes);
        }

        private static void ValidateFile(IFormFile file)
        {
            if (file.Length <= 0) throw new ArgumentException("An uploaded image is empty.");

            if (file.Length > MaxFileSize) throw new ArgumentException("Each ID image must be 5 MB or smaller.");

            var extension = Path.GetExtension(file.FileName);

            if (!AllowedExtensions.Contains(extension)) throw new ArgumentException("Only JPG, JPEG and PNG images are allowed.");
        }


        private async Task<DocumentInfo> SaveFile(
            IFormFile file,
            Guid voterId,
            string documentName,
            List<string> savedPaths)
        {
            var extension = Path.GetExtension(file.FileName).ToLowerInvariant();

            var fileId = Guid.NewGuid();

            var filePath = Path.Combine(_storagePath, $"{fileId}{extension}");

            // Track the path before writing so failure cleanup can
            // remove a partially written file.
            savedPaths.Add(filePath);

            try
            {
                await using var stream = new FileStream(
                    filePath,
                    FileMode.CreateNew,
                    FileAccess.Write,
                    FileShare.None,
                    81920,
                    useAsync: true);

                await file.CopyToAsync(stream);
            }
            catch
            {
                if (File.Exists(filePath))
                    File.Delete(filePath);

                throw;
            }

            return new DocumentInfo
            {
                fileId = fileId,
                voter_id = voterId,
                FileName = documentName,
                fileType = extension,
                uploadDate = DateTime.UtcNow
            };
        }



        public sealed record DocumentFileResult(
            Guid FileId,
            string FileName,
            string ContentType,
            byte[] Content
        );


    }


        
}