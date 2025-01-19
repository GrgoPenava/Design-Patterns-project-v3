package org.uzdiz.userInputChain;

import org.uzdiz.builder.Composition;
import org.uzdiz.ConfigManager;
import org.uzdiz.utils.TableBuilder;
import org.uzdiz.builder.Vehicle;

import java.util.List;
import java.util.stream.Collectors;

public class IKCommand extends CommandHandlerChain {

    @Override
    protected boolean canHandle(String input) {
        return input.matches("^IK(\\s|$).*");
    }

    @Override
    public void execute(String input) {
        ConfigManager config = ConfigManager.getInstance();

        if (!isValidInputFormat(input)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Neispravan format naredbe. Očekuje se format 'IK broj'.");
            return;
        }

        String compositionOznaka = input.substring(3).trim();

        List<Composition> compositions = config.getCompositions().stream()
                .filter(comp -> comp.getOznaka().equals(compositionOznaka))
                .collect(Collectors.toList());

        if (compositions.isEmpty()) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Kompozicija s oznakom " + compositionOznaka + " nije pronađena.");
            return;
        }

        List<Vehicle> compositionVehicles = config.getVehicles().stream()
                .filter(vehicle -> compositions.stream()
                        .anyMatch(comp -> comp.getOznakaVozila().equals(vehicle.getOznaka())))
                .collect(Collectors.toList());

        if (!isValidComposition(compositions)) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Kompozicija mora imati barem jedno pogonsko vozilo i barem jedan vagon.");
            return;
        }

        if (!compositions.get(0).getUloga().equals("P")) {
            config.incrementErrorCount();
            System.out.println("Greška br. " + config.getErrorCount() + ": Na prvom mjestu u kompoziciji mora biti pogonsko vozilo (uloga 'P').");
            return;
        }

        printCompositionTable(compositionVehicles, compositions);
    }

    private boolean isValidInputFormat(String input) {
        return input.matches("^IK \\d+$");
    }

    private boolean isValidComposition(List<Composition> compositions) {
        if (compositions.size() < 2) {
            return false;
        }

        boolean hasPogonskoVozilo = compositions.stream().anyMatch(comp -> comp.getUloga().equals("P"));
        boolean hasVagon = compositions.stream().anyMatch(comp -> comp.getUloga().equals("V"));

        return hasPogonskoVozilo && (hasVagon || compositions.stream().allMatch(comp -> comp.getUloga().equals("P")));
    }

    private void printCompositionTable(List<Vehicle> vehicles, List<Composition> compositions) {
        TableBuilder table = new TableBuilder();
        table.setHeaders("Oznaka", "Uloga", "Opis", "Godina", "Namjena", "Vrsta pogona", "Maks. brzina");

        for (Composition comp : compositions) {
            Vehicle vehicle = vehicles.stream()
                    .filter(v -> v.getOznaka().equals(comp.getOznakaVozila()))
                    .findFirst()
                    .orElse(null);

            if (vehicle != null) {
                table.addRow(
                        vehicle.getOznaka(),
                        comp.getUloga(),
                        vehicle.getOpis(),
                        vehicle.getGodina(),
                        vehicle.getNamjera(),
                        vehicle.getVrstaPogona(),
                        vehicle.getMaksBrzina()
                );
            }
        }

        table.build();
    }
}