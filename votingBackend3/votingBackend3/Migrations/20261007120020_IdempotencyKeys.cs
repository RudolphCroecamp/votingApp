using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class IdempotencyKeys : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "election_type",
                table: "vote");

            migrationBuilder.AddColumn<Guid>(
                name: "idempotency_key",
                table: "vote",
                type: "uuid",
                nullable: false,
                defaultValue: new Guid("00000000-0000-0000-0000-000000000000"));

            migrationBuilder.CreateTable(
                name: "VoteTokenDto",
                columns: table => new
                {
                    token_id = table.Column<Guid>(type: "uuid", nullable: false),
                    Token = table.Column<string>(type: "text", nullable: false),
                    IssuedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_VoteTokenDto", x => x.token_id);
                });

            migrationBuilder.CreateIndex(
                name: "IX_vote_idempotency_key",
                table: "vote",
                column: "idempotency_key",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_VoteTokenDto_token_id",
                table: "VoteTokenDto",
                column: "token_id",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "VoteTokenDto");

            migrationBuilder.DropIndex(
                name: "IX_vote_idempotency_key",
                table: "vote");

            migrationBuilder.DropColumn(
                name: "idempotency_key",
                table: "vote");

            migrationBuilder.AddColumn<string>(
                name: "election_type",
                table: "vote",
                type: "text",
                nullable: false,
                defaultValue: "");
        }
    }
}
