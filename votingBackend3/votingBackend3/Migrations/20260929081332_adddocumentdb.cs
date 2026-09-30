using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class adddocumentdb : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "DocumentInfo",
                columns: table => new
                {
                    fileId = table.Column<Guid>(type: "uuid", nullable: false),
                    originalFileName = table.Column<string>(type: "text", nullable: false),
                    fileType = table.Column<string>(type: "text", nullable: false),
                    fileSize = table.Column<int>(type: "integer", nullable: false),
                    uploadDate = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_DocumentInfo", x => x.fileId);
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "DocumentInfo");
        }
    }
}
