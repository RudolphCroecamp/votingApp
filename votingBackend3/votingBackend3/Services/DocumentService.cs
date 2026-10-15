using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using System.Reflection.Metadata;
using votingBackend3.Data;
using votingBackend3.Models.DTOs;
using votingBackend3.Models.Entities;

namespace votingBackend3.Services
{
    public class DocumentService
    {
        public readonly VotingDbContext _VotingDbContext;

        public DocumentService(VotingDbContext VotingDbContext)
        {
            _VotingDbContext = VotingDbContext;
        }

        //expects id_front and id_back
        public async Task<List<DocumentInfo>> uploadFiles(List<IFormFile> files)
        {

            try
            {
                List<DocumentInfo> documentInfos = new();

                documentInfos.Add( await SaveFile(files[0], "ID_Front") );
                documentInfos.Add(await SaveFile(files[1], "ID_Back"));

                return documentInfos;

            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }
        }


        //return an list of links to the images
        public async Task<List<DocumentInfo>> GetFiles(Guid voter_id)
        {
            voter_id = Guid.Parse("53c1ee50-7a6a-4efe-95e2-ee291ebaed85");//FIX remove this on producton

            try
            {
                //check if file exist
                List<DocumentInfo> documents = await _VotingDbContext.DocumentInfo.Where(d => d.voter_id == voter_id).ToListAsync();

                Console.WriteLine(documents);

                if (documents == null) throw new Exception("No documents found");

                return documents;
            }
            catch (Exception ex)
            {
                throw new Exception(ex.Message);
            }

        }



        //TODO save images to cloud storage
        private async Task<DocumentInfo> SaveFile(IFormFile file, string fileName) {
            //create uploads folder if not exist
            var uploadFolder = Path.Combine(Directory.GetCurrentDirectory(), "VotingDocuments");

            Directory.CreateDirectory(uploadFolder);

            //generate unique file name
            var fileId = Guid.NewGuid();
            var filePathName = fileId + Path.GetExtension(file.FileName);

            
            var filePath = Path.Combine(uploadFolder, filePathName);

            //save file local storage
            var stream = new FileStream(filePath, FileMode.Create);
            await file.CopyToAsync(stream);

            //create file object
            DocumentInfo document = new();

            document.fileId = fileId;
            document.voter_id = Guid.Parse("53c1ee50-7a6a-4efe-95e2-ee291ebaed85");
            document.FileName = fileName;
            document.fileType = Path.GetExtension(file.FileName);
            document.uploadDate = DateTime.UtcNow;

            //save to database
            await _VotingDbContext.DocumentInfo.AddAsync(document);
            await _VotingDbContext.SaveChangesAsync();

            return document;


        }
    }
}
