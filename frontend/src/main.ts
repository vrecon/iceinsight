import { bootstrapApplication } from '@angular/platform-browser';
import { addIcons } from 'ionicons';
import {
  speedometerOutline,
  calendarOutline,
  hardwareChipOutline,
  logOutOutline,
  informationCircleOutline,
  alertCircleOutline,
  addOutline,
  trashOutline,
  refreshOutline,
  personOutline,
  timeOutline,
  trophyOutline,
  menuOutline,
} from 'ionicons/icons';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

addIcons({
  speedometerOutline,
  calendarOutline,
  hardwareChipOutline,
  logOutOutline,
  informationCircleOutline,
  alertCircleOutline,
  addOutline,
  trashOutline,
  refreshOutline,
  personOutline,
  timeOutline,
  trophyOutline,
  menuOutline,
});

bootstrapApplication(AppComponent, appConfig).catch((err) => console.error(err));
