package org.uzdiz.userInputChain;

import org.uzdiz.ConfigManager;
import org.uzdiz.railwayFactory.Railway;
import org.uzdiz.builder.Station;
import org.uzdiz.utils.TableBuilder;

public class IPCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IP(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        TableBuilder table = new TableBuilder();
        table.setHeaders("Oznaka", "Početna stanica", "Završna stanica", "Duljina (km)");
        for (Railway railway : ConfigManager.getInstance().getRailways()) {
            if (railway.getPopisSvihStanica().size() < 2) {
                continue;
            }

            Station firstStation = railway.getPopisSvihStanica().get(0);
            Station lastStation = railway.getPopisSvihStanica().get(railway.getPopisSvihStanica().size() - 1);

            double getSum = railway.getPopisSvihStanica().stream()
                    .mapToDouble(Station::getDuzina)
                    .sum();

            table.addRow(railway.getOznakaPruge(), firstStation.getNaziv(), lastStation.getNaziv(), String.format("%.2f", getSum));
        }
        table.build();
    }
}
