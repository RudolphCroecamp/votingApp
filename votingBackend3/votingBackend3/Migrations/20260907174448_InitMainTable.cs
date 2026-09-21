using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace votingBackend3.Migrations
{
    /// <inheritdoc />
    public partial class InitMainTable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "auth",
                columns: table => new
                {
                    voter_id = table.Column<Guid>(type: "uuid", nullable: false),
                    id_number = table.Column<string>(type: "text", nullable: false),
                    face_embedding = table.Column<double[]>(type: "double precision[]", nullable: false),
                    is_active = table.Column<bool>(type: "boolean", nullable: false),
                    role = table.Column<string>(type: "text", nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_auth", x => x.voter_id);
                });

            migrationBuilder.CreateTable(
                name: "election",
                columns: table => new
                {
                    election_id = table.Column<Guid>(type: "uuid", nullable: false),
                    election_name = table.Column<string>(type: "text", nullable: false),
                    election_type = table.Column<string>(type: "text", nullable: false),
                    start_date = table.Column<DateOnly>(type: "date", nullable: false),
                    end_date = table.Column<DateOnly>(type: "date", nullable: false),
                    isActive = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_election", x => x.election_id);
                });

            migrationBuilder.CreateTable(
                name: "party",
                columns: table => new
                {
                    party_id = table.Column<Guid>(type: "uuid", nullable: false),
                    party_name = table.Column<string>(type: "text", nullable: false),
                    party_description = table.Column<string>(type: "text", nullable: false),
                    status = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_party", x => x.party_id);
                });

            migrationBuilder.CreateTable(
                name: "voter",
                columns: table => new
                {
                    voter_id = table.Column<Guid>(type: "uuid", nullable: false),
                    firstname = table.Column<string>(type: "text", nullable: false),
                    lastname = table.Column<string>(type: "text", nullable: false),
                    province = table.Column<string>(type: "text", nullable: false),
                    city = table.Column<string>(type: "text", nullable: false),
                    street = table.Column<string>(type: "text", nullable: false),
                    postal = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_voter", x => x.voter_id);
                });

            migrationBuilder.CreateTable(
                name: "voting_token",
                columns: table => new
                {
                    token_id = table.Column<Guid>(type: "uuid", nullable: false),
                    TokenHash = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    election_id = table.Column<Guid>(type: "uuid", nullable: false),
                    isUsed = table.Column<bool>(type: "boolean", nullable: false),
                    IssuedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_voting_token", x => x.token_id);
                    table.ForeignKey(
                        name: "FK_voting_token_election_election_id",
                        column: x => x.election_id,
                        principalTable: "election",
                        principalColumn: "election_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "election_party",
                columns: table => new
                {
                    election_id = table.Column<Guid>(type: "uuid", nullable: false),
                    party_id = table.Column<Guid>(type: "uuid", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_election_party", x => new { x.election_id, x.party_id });
                    table.ForeignKey(
                        name: "FK_election_party_election_election_id",
                        column: x => x.election_id,
                        principalTable: "election",
                        principalColumn: "election_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_election_party_party_party_id",
                        column: x => x.party_id,
                        principalTable: "party",
                        principalColumn: "party_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "vote",
                columns: table => new
                {
                    vote_id = table.Column<Guid>(type: "uuid", nullable: false),
                    token_id = table.Column<Guid>(type: "uuid", nullable: false),
                    election_id = table.Column<Guid>(type: "uuid", nullable: false),
                    party_id = table.Column<Guid>(type: "uuid", nullable: false),
                    election_type = table.Column<string>(type: "text", nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_vote", x => x.vote_id);
                    table.ForeignKey(
                        name: "FK_vote_election_election_id",
                        column: x => x.election_id,
                        principalTable: "election",
                        principalColumn: "election_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_vote_party_party_id",
                        column: x => x.party_id,
                        principalTable: "party",
                        principalColumn: "party_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_vote_voting_token_token_id",
                        column: x => x.token_id,
                        principalTable: "voting_token",
                        principalColumn: "token_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "election_voter",
                columns: table => new
                {
                    election_id = table.Column<Guid>(type: "uuid", nullable: false),
                    voter_id = table.Column<Guid>(type: "uuid", nullable: false),
                    vote_id = table.Column<Guid>(type: "uuid", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_election_voter", x => new { x.election_id, x.voter_id });
                    table.ForeignKey(
                        name: "FK_election_voter_election_election_id",
                        column: x => x.election_id,
                        principalTable: "election",
                        principalColumn: "election_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_election_voter_vote_vote_id",
                        column: x => x.vote_id,
                        principalTable: "vote",
                        principalColumn: "vote_id");
                    table.ForeignKey(
                        name: "FK_election_voter_voter_voter_id",
                        column: x => x.voter_id,
                        principalTable: "voter",
                        principalColumn: "voter_id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_election_party_party_id",
                table: "election_party",
                column: "party_id");

            migrationBuilder.CreateIndex(
                name: "IX_election_voter_vote_id",
                table: "election_voter",
                column: "vote_id");

            migrationBuilder.CreateIndex(
                name: "IX_election_voter_voter_id",
                table: "election_voter",
                column: "voter_id");

            migrationBuilder.CreateIndex(
                name: "IX_vote_election_id",
                table: "vote",
                column: "election_id");

            migrationBuilder.CreateIndex(
                name: "IX_vote_party_id",
                table: "vote",
                column: "party_id");

            migrationBuilder.CreateIndex(
                name: "IX_vote_token_id",
                table: "vote",
                column: "token_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_voting_token_election_id",
                table: "voting_token",
                column: "election_id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "auth");

            migrationBuilder.DropTable(
                name: "election_party");

            migrationBuilder.DropTable(
                name: "election_voter");

            migrationBuilder.DropTable(
                name: "vote");

            migrationBuilder.DropTable(
                name: "voter");

            migrationBuilder.DropTable(
                name: "party");

            migrationBuilder.DropTable(
                name: "voting_token");

            migrationBuilder.DropTable(
                name: "election");
        }
    }
}
