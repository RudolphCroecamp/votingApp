using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class HasVotedAttr : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_election_voter_vote_vote_id",
                table: "election_voter");

            migrationBuilder.DropIndex(
                name: "IX_election_voter_vote_id",
                table: "election_voter");

            migrationBuilder.DropColumn(
                name: "vote_id",
                table: "election_voter");

            migrationBuilder.AddColumn<bool>(
                name: "has_voted",
                table: "election_voter",
                type: "boolean",
                nullable: false,
                defaultValue: false);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "has_voted",
                table: "election_voter");

            migrationBuilder.AddColumn<Guid>(
                name: "vote_id",
                table: "election_voter",
                type: "uuid",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_election_voter_vote_id",
                table: "election_voter",
                column: "vote_id");

            migrationBuilder.AddForeignKey(
                name: "FK_election_voter_vote_vote_id",
                table: "election_voter",
                column: "vote_id",
                principalTable: "vote",
                principalColumn: "vote_id");
        }
    }
}
