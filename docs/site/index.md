# GW2ML

GW2ML is a Java library for fast and non-cached access to the data provided
by the Guild Wars 2 game client via the MumbleLink mechanism.

## Usage

GW2ML provides an API that is designed to be as simple and intuitive to use as
possible while remaining efficient. The primary entry-point is [`MumbleLink.open()`](https://gw2toolbelt.github.io/GW2ML/api/com.gw2tb.gw2ml/com/gw2tb/gw2ml/MumbleLink.html#open())
which must be used to open a view of the MumbleLink data before anything can be
read. Once that is done the data can be accessed through various getter methods
on the returned view object.

The following sample prints all data every few seconds:

```java
public class Sample {

    public static void main(String[] args) throws InterruptedException {
        try (MumbleLink mumbleLink = MumbleLink.open()) {
            while (true) {
                float[] buffer = new float[3];

                System.out.println("=== GW2ML Sample ===");
                System.out.printf("uiVersion:\t\t\t\t%s%n", mumbleLink.getUIVersion());
                System.out.printf("uiTick:\t\t\t\t\t%s%n", mumbleLink.getUITick());
                System.out.printf("fAvatarPosition:\t\t%s%n", Arrays.toString(mumbleLink.getAvatarPosition(buffer)));
                System.out.printf("fAvatarFront:\t\t\t%s%n", Arrays.toString(mumbleLink.getAvatarFront(buffer)));
                System.out.printf("fAvatarTop:\t\t\t\t%s%n", Arrays.toString(mumbleLink.getAvatarTop(buffer)));
                System.out.printf("name:\t\t\t\t\t%s%n", mumbleLink.getName());
                System.out.printf("fCameraPosition:\t\t%s%n", Arrays.toString(mumbleLink.getCameraPosition(buffer)));
                System.out.printf("fCameraFront:\t\t\t%s%n", Arrays.toString(mumbleLink.getCameraFront(buffer)));
                System.out.printf("fCameraTop:\t\t\t\t%s%n", Arrays.toString(mumbleLink.getCameraTop(buffer)));
                System.out.printf("identity:\t\t\t\t%s%n", mumbleLink.getIdentity());
                System.out.printf("contextLength:\t\t\t%s%n", mumbleLink.getContextLength());
                System.out.printf("ctx_ServerAddress:\t\t%s%n", mumbleLink.getContext().getServerAddress());
                System.out.printf("ctx_MapID:\t\t\t\t%s%n", mumbleLink.getContext().getMapID());

                long mapType = mumbleLink.getContext().getMapType();
                System.out.printf(
                    "ctx_MapType:\t\t\t%s (%s)%n",
                    mapType, MapType.valueOf(mapType)
                );

                System.out.printf("ctx_ShardID:\t\t\t%s%n", Integer.toBinaryString(mumbleLink.getContext().getShardID()));
                System.out.printf("ctx_Instance:\t\t\t%s%n", mumbleLink.getContext().getInstance());
                System.out.printf("ctx_BuildID:\t\t\t%s%n", mumbleLink.getContext().getBuildID());

                int uiState = mumbleLink.getContext().getUIState();
                System.out.printf(
                    "ctx_UIState:\t\t\t%s (isMapOpen=%s, isCompassTopRight=%s, isCompassRotationEnabled=%s, isGameFocused=%s, isInCompetitiveMode=%s, isTextFieldFocused=%s, isInCombat=%s)%n",
                    Integer.toBinaryString(uiState),
                    UIState.isMapOpen(uiState),
                    UIState.isCompassTopRight(uiState),
                    UIState.isCompassRotationEnabled(uiState),
                    UIState.isGameFocused(uiState),
                    UIState.isInCompetitiveMode(uiState),
                    UIState.isTextFieldFocused(uiState),
                    UIState.isInCombat(uiState)
                );

                System.out.printf("ctx_CompassWidth:\t\t%s%n", mumbleLink.getContext().getCompassWidth());
                System.out.printf("ctx_CompassHeight:\t\t%s%n", mumbleLink.getContext().getCompassHeight());
                System.out.printf("ctx_CompassRotation:\t%s%n", mumbleLink.getContext().getCompassRotation());
                System.out.printf("ctx_PlayerX:\t\t\t%s%n", mumbleLink.getContext().getPlayerX());
                System.out.printf("ctx_PlayerY:\t\t\t%s%n", mumbleLink.getContext().getPlayerY());
                System.out.printf("ctx_MapCenterX:\t\t\t%s%n", mumbleLink.getContext().getMapCenterX());
                System.out.printf("ctx_MapCenterY:\t\t\t%s%n", mumbleLink.getContext().getMapCenterY());
                System.out.printf("ctx_MapScale:\t\t\t%s%n", mumbleLink.getContext().getMapScale());
                System.out.printf("ctx_ProcessID:\t\t\t%s%n", mumbleLink.getContext().getProcessID());

                byte mountType = mumbleLink.getContext().getMountType();
                System.out.printf(
                    "ctx_MountType:\t\t\t%s (%s)%n",
                    mountType, MountType.valueOf(mountType)
                );

                System.out.printf("description:\t\t\t%s%n%n", mumbleLink.getDescription());

                Thread.sleep(Duration.ofSeconds(5));
            }
        }
    }

}
```