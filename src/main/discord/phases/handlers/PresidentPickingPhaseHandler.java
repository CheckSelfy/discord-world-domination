package discord.phases.handlers;

import discord.Session;
import discord.phases.PresidentPickingPhaseLogic;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public class PresidentPickingPhaseHandler extends APhaseEventHandler {
    // private ArrayList<DiscordTeam> teams;
    // private ArrayList<AtomicBoolean> readinness;

    public PresidentPickingPhaseHandler(Session session) {
        super(session, new PresidentPickingPhaseLogic());
        // teams = new ArrayList<>();
        // for (int i = 0; i < uncreatedTeams.size(); i++) {
        // if (!uncreatedTeams.get(i).isEmpty()) {
        // teams.add(new DiscordTeam(uncreatedTeams.get(i),
        // Constants.teamNames.get(i)));
        // }
        // }

        // readinness = new ArrayList<>(teams.size());
        // for (int i = 0; i < readinness.size(); i++) {
        // readinness.set(i, new AtomicBoolean(false));
        // }

        // createCategoryWithDependantds(session.getGuildId()).flatMap(o ->
        // createPolls()).queue();
    }

    // private RestAction<? extends Object> createCategoryWithDependantds(long
    // guildId) {
    // Guild guild = session.getJDA().getGuildById(guildId);

    // return guild
    // .createCategory(Constants.bundle.getString("game_name"))
    // .flatMap(category -> guild
    // .modifyCategoryPositions()
    // .selectPosition(category)
    // .moveTo(1)
    // .map(v -> category))
    // .flatMap(category -> {
    // List<RestAction<Void>> actions = new ArrayList<>(teams.size());
    // for (DiscordTeam team : teams) {
    // actions.add(
    // guild.createRole()
    // .setName(team.getLocalization().getName())
    // .setColor(team.getLocalization().getColor()) // created Role
    // .flatMap(role -> {
    // team.setGuildId(role.getGuild().getIdLong());
    // team.setRoleId(role.getIdLong());

    // return addRoles(role, team) // added users
    // .and(createTeamVoiceChannel(role, category, team)); // created voice
    // }));
    // }
    // return RestAction.allOf(actions);
    // });

    // }

    // private RestAction<Role> addRoles(Role role, Team team) {
    // Guild guild = role.getGuild();
    // List<RestAction<Void>> actions = new ArrayList<>();
    // for (Member member : team.getMembers()) {
    // actions.add(
    // guild.addRoleToMember(getJDA().getUserById(member.getUserId()), role));
    // }
    // return RestAction.allOf(actions).map(listOfVoids -> role);
    // }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        //
        //
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {

    }

    public void nextPhase() { /* session.setPhase(new TalkingPhase(session)); */ }
}
