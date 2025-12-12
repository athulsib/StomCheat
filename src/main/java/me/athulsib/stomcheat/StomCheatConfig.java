package me.athulsib.stomcheat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StomCheatConfig {
    private int threadCount = Math.min(Runtime.getRuntime().availableProcessors(), 16);

    private String alert = "&6&lStomCheat &7&o>> &6%player% &fhas failed &6%check% %type% &8[VL:&r%vl%&7/%punishvl%&8] %experimental%";
    private String experimental = "&c(DEV)";

    private String hover =
            """
            &6Details:
            &eCheck: &a%check%
            &eType: &a%type%
            &eViolations: &a%vl%
            &ePing: &a%ping%ms
            &eDescription: &a%description%
            &eData: &a%data%
            """;

    String broadcast =
            """
            
            &6&lStomCheat &8>> &e%s &bhas been removed from the Network
            &eReason: &cUnfair Advantage
            
            """;

    String punishKick =
            """

            &cYou have been removed from the Network
            &c[StomCheat] Unfair Advantage

            """;

}
