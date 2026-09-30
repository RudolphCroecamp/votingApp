using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class adddocumentdb6 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_DocumentInfo_voter_fileId",
                table: "DocumentInfo");

            migrationBuilder.AddColumn<Guid>(
                name: "voter_id",
                table: "DocumentInfo",
                type: "uuid",
                nullable: false,
                defaultValue: new Guid("00000000-0000-0000-0000-000000000000"));

            migrationBuilder.CreateIndex(
                name: "IX_DocumentInfo_voter_id",
                table: "DocumentInfo",
                column: "voter_id");

            migrationBuilder.AddForeignKey(
                name: "FK_DocumentInfo_voter_voter_id",
                table: "DocumentInfo",
                column: "voter_id",
                principalTable: "voter",
                principalColumn: "voter_id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_DocumentInfo_voter_voter_id",
                table: "DocumentInfo");

            migrationBuilder.DropIndex(
                name: "IX_DocumentInfo_voter_id",
                table: "DocumentInfo");

            migrationBuilder.DropColumn(
                name: "voter_id",
                table: "DocumentInfo");

            migrationBuilder.AddForeignKey(
                name: "FK_DocumentInfo_voter_fileId",
                table: "DocumentInfo",
                column: "fileId",
                principalTable: "voter",
                principalColumn: "voter_id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
